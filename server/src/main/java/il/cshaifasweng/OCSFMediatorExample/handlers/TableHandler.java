package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Reservation;
import il.cshaifasweng.OCSFMediatorExample.entities.RestaurantTable;
import il.cshaifasweng.OCSFMediatorExample.server.SimpleServer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TableHandler {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void reserveTable(RestaurantTable tableToReserve, SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            RestaurantTable table = session.get(RestaurantTable.class, tableToReserve.getId());
            if (table != null && !table.isReserved()) {
                Reservation reservation = new Reservation(table.getBranch(), LocalDate.now(),
                        LocalTime.now(), table.getCapacity(), table.getSeatingArea(), "name", "0", "email", "0", table);
                ReservationHandler.saveReservation(reservation, sessionFactory);
                table.setReserved(true);
                session.update(table);
                transaction.commit();
                System.out.println("Table " + table.getTableNumber() + " reserved successfully.");
                scheduleAutoRelease(table.getId(), sessionFactory);
            } else {
                System.out.println("Table not available or not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void scheduleAutoRelease(int tableId, SessionFactory sessionFactory) {
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
                            new il.cshaifasweng.OCSFMediatorExample.entities.Message(table, "#TableReservationCanceledSuccess")
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
