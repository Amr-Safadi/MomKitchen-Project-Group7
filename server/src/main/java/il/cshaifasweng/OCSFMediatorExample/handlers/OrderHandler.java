package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Orders;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class OrderHandler {

    public static void placeOrder(Orders order , SessionFactory sessionFactory) {
        Transaction transaction = null;
        Session session = sessionFactory.openSession();
        try {
            transaction = session.beginTransaction();

           // order.printOrder(); // Debugging: Print the order details
            session.save(order); // Save the order (DO NOT use saveOrUpdate)
            transaction.commit(); // Commit the transaction

            System.out.println("✅ Order placed successfully: " + order.getName() + " - $" + order.getTotalPrice());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback(); // Rollback if there's an error
            }
            System.out.println("❌ Error placing order:");
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close(); // Close the session to free resources
            }
        }
    }
}
