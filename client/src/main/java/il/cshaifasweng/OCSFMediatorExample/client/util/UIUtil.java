package il.cshaifasweng.OCSFMediatorExample.client.util;

import javafx.scene.control.TextField;

public class UIUtil {
    public static void styleTextField(TextField tf) {
        tf.setStyle("-fx-font-weight: bold; -fx-background-color: white; -fx-text-fill: black;");
    }

    public static boolean isValidPrice(String priceInput) {
        try {
            double price = Double.parseDouble(priceInput);
            return price > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
