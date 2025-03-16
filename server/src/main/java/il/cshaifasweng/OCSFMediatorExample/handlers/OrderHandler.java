package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Orders;
import il.cshaifasweng.OCSFMediatorExample.util.EmailSender;
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

            // Send email notification
            String emailSubject = "🍽️ Order Confirmation - Mom's Kitchen";
            String emailBody = "Dear Customer,\n\n"
                    + "Thank you for placing an order with Mom's Kitchen! Your order has been successfully received.\n"
                    + "Order ID: " + order.getId() + "\n"
                    + "Total Amount: $" + order.getTotalPrice() + "\n\n"
                    + "Your delicious meal is being prepared and will be on its way soon! \n\n"
                    + "Best regards,\n"
                    + "Mom's Kitchen Team";

            System.out.println("✅ Order placed successfully: " + order.getName() + " - $" + order.getTotalPrice());
            EmailSender.sendEmail(order.getEmail(), emailSubject, emailBody);
            System.out.println("📧 Order confirmation email sent to " + order.getEmail());


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
