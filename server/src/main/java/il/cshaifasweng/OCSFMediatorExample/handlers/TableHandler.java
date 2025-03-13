package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.RestaurantTable;
import il.cshaifasweng.OCSFMediatorExample.server.SimpleServer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

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
                table.setReserved(true);
                session.update(table);
                transaction.commit();
                System.out.println("Table " + table.getTableNumber() + " reserved successfully.");
                // Schedule auto-release after 90 minutes.
                scheduleAutoRelease(table.getId(), sessionFactory);
            } else {
                System.out.println("Table not available or not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void scheduleAutoRelease(int tableId, SessionFactory sessionFactory) {
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
