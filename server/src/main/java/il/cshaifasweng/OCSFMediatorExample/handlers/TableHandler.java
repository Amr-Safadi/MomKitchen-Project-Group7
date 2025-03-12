package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.RestaurantTable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class TableHandler {
    public static void reserveTable(RestaurantTable tableToReserve, SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            RestaurantTable table = session.get(RestaurantTable.class, tableToReserve.getId());
            if (table != null && !table.isReserved()) {
                table.setReserved(true);
                session.update(table);
                transaction.commit();
                System.out.println("Table " + table.getTableNumber() + " reserved successfully.");
            } else {
                System.out.println("Table not available or not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
