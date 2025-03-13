package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Orders;
import il.cshaifasweng.OCSFMediatorExample.util.EmailSender;
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
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        try {
            // Get the refund amount based on cancellation rules
            double refundAmount = calculateRefund(order);

            // Delete the order from the database
            session.delete(order);
            transaction.commit();
            System.out.println("❌ Order ID " + order.getId() + " has been canceled.");

            // Send email notification
            String emailSubject = "Your Order Cancellation Confirmation";
            String emailBody = "Dear Customer,\n\n"
                    + "Your order with ID " + order.getId() + " has been successfully canceled.\n"
                    + "Refund Amount: $" + refundAmount + "\n\n"
                    + "Thank you for using Mom's Kitchen!";

            EmailSender.sendEmail(order.getEmail(), emailSubject, emailBody);
            System.out.println("📧 Email sent to " + order.getEmail());

        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    private static double calculateRefund(Orders order) {
        long hoursLeft = java.time.Duration.between(java.time.LocalDateTime.now(), order.getDeliveryTime()).toHours();
        if (hoursLeft >= 3) {
            return order.getTotalPrice();  // Full refund
        } else if (hoursLeft >= 1) {
            return order.getTotalPrice() * 0.5;  // 50% refund
        } else {
            return 0;  // No refund
        }
    }
}
