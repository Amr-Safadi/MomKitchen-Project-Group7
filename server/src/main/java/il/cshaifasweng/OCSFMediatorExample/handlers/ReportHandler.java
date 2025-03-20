package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class ReportHandler {

    public static List<Orders> getDeliveryOrders(SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            List<Orders> orders = session.createQuery("FROM Orders WHERE orderType = 'Delivery'", Orders.class).getResultList();
            System.out.println("🔍 Delivery Orders Retrieved: " + orders.size()); // Debug print
            return orders;
        }
    }


    public static List<Object[]> getTableReservations(SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            List<Object[]> reservations = session.createQuery(
                    "SELECT DATE(date), COUNT(*) FROM Reservation GROUP BY DATE(date)", Object[].class).getResultList();
            System.out.println("🔍 Reservations Retrieved: " + reservations.size()); // Debug print
            return reservations;
        }
    }


    public static List<Object[]> getComplaintHistogram(SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            List<Object[]> complaints = session.createQuery(
                    "SELECT DATE(submittedAt), COUNT(*) FROM ContactRequest GROUP BY DATE(submittedAt)", Object[].class).getResultList();
            System.out.println("🔍 Complaints Retrieved: " + complaints.size()); // Debug print
            return complaints;
        }
    }

}
