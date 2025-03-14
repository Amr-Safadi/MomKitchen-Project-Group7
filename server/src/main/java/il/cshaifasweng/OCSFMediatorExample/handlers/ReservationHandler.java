package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Reservation;
import il.cshaifasweng.OCSFMediatorExample.entities.Branch;
import il.cshaifasweng.OCSFMediatorExample.entities.RestaurantTable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.Duration;
import java.time.LocalDateTime;
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

            LocalDateTime reqStart = LocalDateTime.of(reservation.getDate(), reservation.getTime());
            LocalDateTime reqEnd = reqStart.plusMinutes(90);

            for (RestaurantTable table : candidateTables) {
                List<Reservation> reservations = session.createQuery(
                                "FROM Reservation r WHERE r.table.id = :tableId AND r.date = :date", Reservation.class)
                        .setParameter("tableId", table.getId())
                        .setParameter("date", reservation.getDate())
                        .getResultList();

                boolean conflict = false;
                for (Reservation r : reservations) {
                    LocalDateTime existingStart = LocalDateTime.of(r.getDate(), r.getTime());
                    LocalDateTime existingEnd = existingStart.plusMinutes(90);
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

            LocalDateTime requestedDateTime = LocalDateTime.of(reservation.getDate(), reservation.getTime());
            LocalDateTime now = LocalDateTime.now();
            if (!requestedDateTime.isAfter(now)) {
                allocatedTable.setReserved(true);
                session.update(allocatedTable);
            } else {
                long delayMillis = Duration.between(now, requestedDateTime).toMillis();
                ReservationScheduler.scheduleReservationActivation(allocatedTable.getId(), delayMillis, sessionFactory);
            }
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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime branchOpenDT = LocalDateTime.of(now.toLocalDate(), branch.getOpenHour()).plusMinutes(15);
        LocalDateTime branchCloseDT = LocalDateTime.of(now.toLocalDate(), branch.getCloseHour()).minusMinutes(60);
        List<String> alternatives = new ArrayList<>();
        LocalDateTime slot = branchOpenDT;
        while (!slot.isAfter(branchCloseDT)) {
            alternatives.add(slot.toLocalTime().toString());
            slot = slot.plusMinutes(15);
        }
        return String.join(", ", alternatives);
    }
}
