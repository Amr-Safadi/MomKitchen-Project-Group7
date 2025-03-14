package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.RestaurantTable;
import il.cshaifasweng.OCSFMediatorExample.server.SimpleServer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReservationScheduler {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void scheduleReservationActivation(int tableId, long delayMillis, SessionFactory sessionFactory) {
        scheduler.schedule(() -> {
            try (Session session = sessionFactory.openSession()) {
                Transaction tx = session.beginTransaction();
                RestaurantTable table = session.get(RestaurantTable.class, tableId);
                if (table != null && !table.isReserved()) {
                    table.setReserved(true);
                    session.update(table);
                    tx.commit();
                    System.out.println("Activated reservation for table " + table.getTableNumber());
                    SimpleServer.getInstance().sendToAllClients(
                            new il.cshaifasweng.OCSFMediatorExample.entities.Message(table, "#TableReservedSuccess")
                    );
                    TableHandler.scheduleAutoRelease(tableId, sessionFactory);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
    }
}
