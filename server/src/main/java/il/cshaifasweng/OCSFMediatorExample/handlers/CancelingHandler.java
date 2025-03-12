package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Orders;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class CancelingHandler {

    public static List<Orders> fetchOrders(String email, String phone, SessionFactory sessionFactory) {
        List<Orders> userOrders = null;

        Session session = sessionFactory.openSession();
        try {
            // Use DISTINCT to prevent duplicate orders when meals are fetched
            userOrders = session.createQuery(
                            "SELECT DISTINCT o FROM Orders o LEFT JOIN FETCH o.meals " +
                                    "WHERE o.email = :email AND o.phoneNumber = :phone " +
                                    "ORDER BY o.orderPlacedTime DESC", Orders.class)
                    .setParameter("email", email)
                    .setParameter("phone", phone)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return userOrders; // Meals are already loaded before session closes
    }
    public static void cancelOrder(Orders order, SessionFactory sessionFactory) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            // Fetch the order from the database
            Orders orderToDelete = session.get(Orders.class, order.getId());

            if (orderToDelete != null) {
                session.delete(orderToDelete);
                transaction.commit(); // Save changes
                System.out.println("✅ Order ID " + order.getId() + " has been successfully deleted.");
            } else {
                System.out.println("⚠️ Order ID " + order.getId() + " not found in the database.");
            }

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback(); // Rollback in case of failure
            }
            e.printStackTrace();
            System.out.println("❌ Failed to cancel order ID " + order.getId());
        }
    }


}
