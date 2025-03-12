package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Orders;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.util.List;

public class CancelingHandler {

    public static List<Orders> fetchOrders(String email, String phone, SessionFactory sessionFactory) {
        List<Orders> userOrders = null;

        try (Session session = sessionFactory.openSession()) {
            // Fetch all orders of the user
            userOrders = session.createQuery(
                            "FROM Orders WHERE email = :email AND phoneNumber = :phone ORDER BY orderPlacedTime DESC", Orders.class)
                    .setParameter("email", email)
                    .setParameter("phone", phone)
                    .getResultList();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return userOrders; // Return the fetched orders list
    }
}
