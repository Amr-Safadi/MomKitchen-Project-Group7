package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.ContactRequest;
import il.cshaifasweng.OCSFMediatorExample.util.EmailSender;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class ComplaintHandler {

    public static void saveComplaint(ContactRequest complaint, SessionFactory sessionFactory) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(complaint);
            transaction.commit();
            System.out.println("✅ Complaint saved successfully.");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            System.out.println("❌ Error saving the complaint.");
        }
    }


    // ✅ Update a complaint when it gets resolved
    public static void resolveComplaint(ContactRequest complaint, SessionFactory sessionFactory) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.update(complaint);  // ✅ Update complaint instead of saving a new one
            transaction.commit();
            System.out.println("✅ Complaint resolved successfully.");

            // ✅ Send resolution email after successfully updating
            sendResolutionEmail(complaint);

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            System.out.println("❌ Error resolving the complaint.");
        }
    }

    public static void sendResolutionEmail(ContactRequest complaint) {
        System.out.println("📧 Attempting to send email to: " + complaint.getEmail());

        String subject = "Complaint Resolution - Mom's Kitchen";
        String refundText = complaint.isRefundIssued() ?
                "You have been granted a refund of $" + complaint.getRefundAmount() + "." : "No refund was issued.";

        String message = String.format(
                "Dear %s,\n\nYour complaint has been reviewed and resolved.\n\nResolution: %s\n\n%s\n\nThank you for reaching out to us.\nBest Regards,\nMom's Kitchen",
                complaint.getName(), complaint.getResolutionScript(), refundText
        );

        EmailSender.sendEmail(complaint.getEmail(), subject, message);
        System.out.println("✅ Email sent successfully.");
    }

}
