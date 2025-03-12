package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Reservation;
import il.cshaifasweng.OCSFMediatorExample.entities.Branch;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationHandler {
    public static void handleReservation(Reservation reservationRequest,
                                         org.hibernate.SessionFactory sessionFactory) {
    }

    public static boolean checkAvailability(Reservation reservation, SessionFactory sessionFactory) {
        Branch branch = reservation.getBranch();
        if (branch == null) {
            System.out.println("Branch is null in reservation.");
            return false;
        }
        LocalTime branchOpen = branch.getOpenHour();
        LocalTime branchClose = branch.getCloseHour();
        LocalTime startAllowed = branchOpen.plusMinutes(15);
        LocalTime endAllowed = branchClose.minusMinutes(60);
        LocalTime requested = reservation.getTime();
        boolean withinRange = !requested.isBefore(startAllowed) && !requested.isAfter(endAllowed);
        boolean isSlotFree = true;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            List<Reservation> reservations = session.createQuery(
                            "FROM Reservation r WHERE r.branch.id = :branchId AND r.date = :date AND r.time = :time", Reservation.class)
                    .setParameter("branchId", branch.getId())
                    .setParameter("date", reservation.getDate())
                    .setParameter("time", requested)
                    .getResultList();
            tx.commit();
            if (reservations != null && !reservations.isEmpty()) {
                isSlotFree = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return withinRange && isSlotFree;
    }

    public static void saveReservation(Reservation reservation, SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            if (reservation.getBranch() == null || reservation.getBranch().getId() == 0) {
                String branchName = reservation.getBranch() != null ? reservation.getBranch().getName() : "defaultBranch";
                Branch persistentBranch = session.createQuery("FROM Branch WHERE name = :branchName", Branch.class)
                        .setParameter("branchName", branchName)
                        .uniqueResult();
                if (persistentBranch == null) {
                    throw new RuntimeException("Branch not found: " + branchName);
                }
                reservation.setBranch(persistentBranch);
            }
            session.save(reservation);
            tx.commit();
            System.out.println("Reservation saved: " + reservation.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
