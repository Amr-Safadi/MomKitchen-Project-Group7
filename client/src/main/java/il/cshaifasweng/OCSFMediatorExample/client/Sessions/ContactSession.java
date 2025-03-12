package il.cshaifasweng.OCSFMediatorExample.client.Sessions;

import il.cshaifasweng.OCSFMediatorExample.entities.ContactRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class ContactSession {

    private static final String URL = "jdbc:mysql://localhost:3306/MomKitchen?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Aboode1212@";

    public static void saveToDatabase(ContactRequest request) {
        String sql = "INSERT INTO contact_requests (name, branch, complaint) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, request.getName());
            stmt.setString(2, request.getBranch());
            stmt.setString(3, request.getComplaint());

            stmt.executeUpdate();
            System.out.println("Contact request saved to database.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
