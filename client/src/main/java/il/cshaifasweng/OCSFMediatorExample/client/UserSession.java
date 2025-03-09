package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.User;

public class UserSession {
    private static User currentUser = null;

    public static void setUser(User user) {
        currentUser = user;
        System.out.println("UserSesson has been set to " + currentUser);
    }

    public static User getUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
        System.out.println("UserSesson has been removed from " + currentUser);
    }
}
