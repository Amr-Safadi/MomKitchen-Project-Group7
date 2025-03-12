package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.util.EncryptionUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class UserHandler {
    public static void handleLogin(User user, ConnectionToClient client, SessionFactory sessionFactory,
                                   ConcurrentHashMap<ConnectionToClient, String> onlineUsers) {
        Transaction tx = null;
        User authenticatedUser = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            authenticatedUser = session.createQuery("FROM User WHERE email = :email", User.class)
                    .setParameter("email", user.getEmail())
                    .uniqueResult();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }

        try {
            if (authenticatedUser == null) {
                client.sendToClient(new Message(null, "#EmailNotFound"));
                System.out.println("Login failed: Email not found -> " + user.getEmail());
            } else if (!authenticatedUser.getPassword().equals(EncryptionUtil.passwordEncrypt(user.getPassword()))) {
                client.sendToClient(new Message(null, "#IncorrectPassword"));
                System.out.println("Login failed: Incorrect password for -> " + user.getEmail());
            } else if (onlineUsers.containsValue(authenticatedUser.getEmail())) {
                client.sendToClient(new Message(null, "#AlreadyLoggedIn"));
                System.out.println("Login failed: User already logged in -> " + user.getEmail());
            } else {
                onlineUsers.put(client, authenticatedUser.getEmail());
                client.sendToClient(new Message(authenticatedUser, "#LoginSuccess"));
                System.out.println("User logged in: " + authenticatedUser.getEmail() + " | Role: " + authenticatedUser.getRole());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void populateUsers(Session session) {
        User Amr = new User("amr", EncryptionUtil.passwordEncrypt("Amr123"), "Amr Safadi", User.Role.GENERAL_MANAGER);
        User Marian = new User("marian", EncryptionUtil.passwordEncrypt("Marian123"), "Marian Dahmoush", User.Role.BRANCH_MANAGER);
        User Kanar = new User("kanar", EncryptionUtil.passwordEncrypt("Kanar123"), "Kanar Arrabi", User.Role.DIETITIAN);

        session.saveOrUpdate(Amr);
        session.saveOrUpdate(Marian);
        session.saveOrUpdate(Kanar);
        System.out.println("Users added to the database.");
    }
}
