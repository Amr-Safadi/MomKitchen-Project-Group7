package il.cshaifasweng.OCSFMediatorExample.client.util;

import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

public class BackgroundUtil {

    private static final Map<String, String> MEAL_IMAGES = new HashMap<>();
    static {
        MEAL_IMAGES.put("Pizza", "/Images/pizza.jpg");
        MEAL_IMAGES.put("Burger", "/Images/burger.jpg");
        MEAL_IMAGES.put("Pasta", "/Images/pasta.jpg");
        MEAL_IMAGES.put("Mineral Water", "/Images/water.jpg");
        MEAL_IMAGES.put("Diet Coke", "/Images/coke.jpg");
        MEAL_IMAGES.put("Orange juice", "/Images/juice.jpg");
        MEAL_IMAGES.put("Fillet Steak", "/Images/filletSteak.jpg");
        MEAL_IMAGES.put("Chicken Wings", "/Images/chickenWings.jpg");
        MEAL_IMAGES.put("cheese Ravioli", "/Images/ravioli.jpg");
        MEAL_IMAGES.put("Sezar Salad", "/Images/seafood.jpg");
    }

    public static AnchorPane createMealImagePane(Meals meal) {
        String imagePath = MEAL_IMAGES.get(meal.getName());
        if (imagePath == null) {
            return null;
        }
        Image mealImage = new Image(BackgroundUtil.class.getResource(imagePath).toExternalForm());
        ImageView imageView = new ImageView(mealImage);
        imageView.setOpacity(0.3);
        imageView.setFitWidth(600);
        imageView.setFitHeight(600);
        imageView.setPreserveRatio(false);
        AnchorPane imagePane = new AnchorPane();
        imagePane.getChildren().add(imageView);
        AnchorPane.setTopAnchor(imageView, 330.0);
        AnchorPane.setLeftAnchor(imageView, -50.0);
        return imagePane;
    }

    public static Background createTransparentBackground() {
        BackgroundFill fill = new BackgroundFill(Color.rgb(255, 255, 255, 0.0), CornerRadii.EMPTY, Insets.EMPTY);
        return new Background(fill);
    }

    public static void setPaneBackground(AnchorPane pane, String imagePath) {
        Image bgImage = new Image(BackgroundUtil.class.getResource(imagePath).toExternalForm());
        BackgroundImage background = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(
                        BackgroundSize.AUTO,
                        BackgroundSize.AUTO,
                        true,
                        true,
                        true,
                        false
                )
        );
        pane.setBackground(new Background(background));
    }
}
