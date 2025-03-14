package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Reservation;
import il.cshaifasweng.OCSFMediatorExample.entities.Branch;
import il.cshaifasweng.OCSFMediatorExample.entities.RestaurantTable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

public class ReservationHandler {

    public static RestaurantTable allocateTableForReservation(Reservation reservation, SessionFactory sessionFactory) {
        RestaurantTable allocatedTable = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Branch branch = reservation.getBranch();
            List<RestaurantTable> candidateTables = session.createQuery(
                            "FROM RestaurantTable t WHERE t.branch.id = :branchId AND t.capacity >= :guestCount ORDER BY t.capacity ASC",
                            RestaurantTable.class)
                    .setParameter("branchId", branch.getId())
                    .setParameter("guestCount", reservation.getGuests())
                    .getResultList();

            LocalTime reqStart = reservation.getTime();
            LocalTime reqEnd = reqStart.plusMinutes(90);

            for (RestaurantTable table : candidateTables) {
                List<Reservation> reservations = session.createQuery(
                                "FROM Reservation r WHERE r.table.id = :tableId AND r.date = :date", Reservation.class)
                        .setParameter("tableId", table.getId())
                        .setParameter("date", reservation.getDate())
                        .getResultList();

                boolean conflict = false;
                for (Reservation r : reservations) {
                    LocalTime existingStart = r.getTime();
                    LocalTime existingEnd = existingStart.plusMinutes(90);
                    if (reqStart.isBefore(existingEnd) && existingStart.isBefore(reqEnd)) {
                        conflict = true;
                        break;
                    }
                }
                if (!conflict) {
                    allocatedTable = table;
                    break;
                }
            }
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allocatedTable;
    }

    public static boolean saveReservation(Reservation reservation, SessionFactory sessionFactory) {
        boolean success = false;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            RestaurantTable allocatedTable = allocateTableForReservation(reservation, sessionFactory);
            if (allocatedTable == null) {
                tx.rollback();
                return false;
            }
            reservation.setTable(allocatedTable);
            allocatedTable.setReserved(true);
            session.update(allocatedTable);
            session.save(reservation);
            tx.commit();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public static String computeAlternativeTimes(Reservation reservation) {
        Branch branch = reservation.getBranch();
        if (branch == null) {
            return "Branch not specified";
        }
        LocalTime branchOpen = branch.getOpenHour();
        LocalTime branchClose = branch.getCloseHour();
        LocalTime startAllowed = branchOpen.plusMinutes(15);
        LocalTime endAllowed = branchClose.minusMinutes(60);
        List<String> alternatives = new ArrayList<>();
        LocalTime slot = startAllowed;
        while (!slot.isAfter(endAllowed)) {
            alternatives.add(slot.toString());
            slot = slot.plusMinutes(15);
        }
        return String.join(", ", alternatives);
    }
}
