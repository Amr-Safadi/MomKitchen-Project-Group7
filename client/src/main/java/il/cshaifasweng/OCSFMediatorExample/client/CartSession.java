package il.cshaifasweng.OCSFMediatorExample.entities;

public class CartSession {
    private static Cart cart = new Cart(); // Single instance of cart

    private CartSession() {
        // Private constructor to prevent instantiation
    }

    // Get the cart instance
    public static Cart getCart() {
        return cart;
    }

    // Clear the cart session (e.g., after checkout)
    public static void clearCart() {
        cart.clearCart();
    }
}
