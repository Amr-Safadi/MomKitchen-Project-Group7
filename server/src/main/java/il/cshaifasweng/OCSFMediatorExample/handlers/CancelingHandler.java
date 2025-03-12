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


}
