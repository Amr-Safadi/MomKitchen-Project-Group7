package il.cshaifasweng.OCSFMediatorExample.client.Sessions;

import il.cshaifasweng.OCSFMediatorExample.entities.Cart;

public class CartSession {
    private static Cart cart = new Cart();
    private CartSession() {}
    public static Cart getCart() {
        return cart;
    }
    public static void clearCart() {
        cart.clearCart();
    }
}
