package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.Reservation;
import il.cshaifasweng.OCSFMediatorExample.entities.RestaurantTable;
import il.cshaifasweng.OCSFMediatorExample.server.SimpleServer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TableHandler {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static RestaurantTable reserveTable(RestaurantTable tableToReserve, SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            RestaurantTable table = session.get(RestaurantTable.class, tableToReserve.getId());
            if (table == null || table.isReserved()) {
                System.out.println("Table not available or not found.");
                transaction.rollback();
                return null;
            }

            LocalDate today = LocalDate.now();
            LocalTime newStart = LocalTime.now();
            LocalTime newEnd = newStart.plusMinutes(90);

            List<Reservation> existingReservations = session.createQuery(
                            "SELECT r FROM Reservation r JOIN r.tables t WHERE t.id = :tableId AND r.date = :today",
                            Reservation.class)
                    .setParameter("tableId", table.getId())
                    .setParameter("today", today)
                    .getResultList();

            for (Reservation r : existingReservations) {
                LocalTime existingStart = r.getTime();
                LocalTime existingEnd = existingStart.plusMinutes(90);
                if (newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd)) {
                    System.out.println("Table " + table.getTableNumber() + " already has a conflicting reservation.");
                    transaction.rollback();
                    return null;
                }
            }

            // Create and save the reservation if no conflict is found.
            Reservation reservation = new Reservation(
                    table.getBranch(),
                    today,
                    newStart,
                    table.getCapacity(),
                    table.getSeatingArea(),
                    "name", "0", "email", "0",
                    table
            );
            ReservationHandler.saveReservation(reservation, sessionFactory);

            table.setReserved(true);
            session.update(table);
            transaction.commit();
            System.out.println("Table " + table.getTableNumber() + " reserved successfully.");
            scheduleAutoRelease(table.getId(), sessionFactory);
            return table;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void scheduleAutoRelease(int tableId, SessionFactory sessionFactory) {
        scheduler.schedule(() -> {
            try (Session session = sessionFactory.openSession()) {
                Transaction transaction = session.beginTransaction();
                RestaurantTable table = session.get(RestaurantTable.class, tableId);
                if (table != null && table.isReserved()) {
                    table.setReserved(false);
                    session.update(table);
                    transaction.commit();
                    System.out.println("Auto-released table " + table.getTableNumber() + " after 90 minutes.");
                    SimpleServer.getInstance().sendToAllClients(
                            new Message(table, "#TableReservationCanceledSuccess")
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 90, TimeUnit.MINUTES);
    }

    public static void cancelTableReservation(RestaurantTable tableToCancel, SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            RestaurantTable table = session.get(RestaurantTable.class, tableToCancel.getId());
            if (table != null && table.isReserved()) {
                Reservation reservation = session.createQuery(
                                "SELECT r FROM Reservation r JOIN r.tables t WHERE t.id = :tableId AND r.date = :date",
                                Reservation.class)
                        .setParameter("tableId", table.getId())
                        .setParameter("date", LocalDate.now())
                        .uniqueResult();

                if (reservation != null) {
                    reservation.getTables().remove(table);
                    if (reservation.getTables().isEmpty()) {
                        session.delete(reservation);
                        System.out.println("Reservation deleted for table " + table.getTableNumber());
                    } else {
                        session.update(reservation);
                        System.out.println("Reservation updated for table " + table.getTableNumber());
                    }
                }
                table.setReserved(false);
                session.update(table);
                transaction.commit();
                System.out.println("Table " + table.getTableNumber() + " reservation canceled.");
            } else {
                System.out.println("Table not reserved or not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
