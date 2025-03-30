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


    public static AnchorPane createMealImagePaneFromImage(Image mealImage) {
        ImageView imageView = new ImageView(mealImage);
        imageView.setOpacity(0.3);
        imageView.setFitWidth(600);
        imageView.setFitHeight(600);
        imageView.setPreserveRatio(false);

        AnchorPane imagePane = new AnchorPane(imageView);
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
