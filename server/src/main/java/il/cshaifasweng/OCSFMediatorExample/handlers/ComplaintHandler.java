package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.ContactRequest;
import il.cshaifasweng.OCSFMediatorExample.util.EmailSender;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.LocalDateTime;

public class ComplaintHandler {

    public static void saveComplaint(ContactRequest complaint, SessionFactory sessionFactory) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(complaint);
            transaction.commit();
            System.out.println("✅ Complaint saved successfully.");


            // Send email notification
            String emailSubject = "We've Received Your Complaint – Mom's Kitchen Support";
            String emailBody = "Dear Customer,\n\n"
                    + "We have received your complaint and our support team is currently reviewing it.\n"
                    + "Complaint ID: " + complaint.getId() + "\n"
                    + "Submitted on: " + LocalDateTime.now() + "\n\n"
                    + "We appreciate your patience and will get back to you as soon as possible.\n\n"
                    + "Best regards,\n"
                    + "Mom's Kitchen Support Team";


            EmailSender.sendEmail(complaint.getEmail(), emailSubject, emailBody);
            System.out.println("📧 Email sent to " + complaint.getEmail());

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
