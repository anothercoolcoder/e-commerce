package uptc.viewController.components;

import java.util.Locale;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/** Calificación en estrellas. Siempre muestra también el valor numérico. */
public class RatingStars extends HBox {

    public RatingStars(double rating, int reviews) {
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (rating >= i) {
                stars.append("★");
            } else if (rating >= i - 0.5) {
                stars.append("⯨");
            } else {
                stars.append("☆");
            }
        }
        String text = stars + "  " + String.format(Locale.ROOT, "%.1f", rating);
        if (reviews > 0) {
            text += "  (" + reviews + ")";
        }
        Label value = new Label(text);
        value.getStyleClass().add("rating");
        getChildren().add(value);
    }
}
