package il.cshaifasweng.OCSFMediatorExample.util;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import il.cshaifasweng.OCSFMediatorExample.entities.Branch;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.entities.Reservation;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() throws HibernateException {
        Configuration configuration = new Configuration();

        configuration.addAnnotatedClass(Meals.class);
        configuration.addAnnotatedClass(Branch.class);
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Reservation.class);

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();

        return configuration.buildSessionFactory(serviceRegistry);
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
