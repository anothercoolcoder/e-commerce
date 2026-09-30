package uptc.viewController.components;

import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/** Botón de corazón que se marca y desmarca con una pequeña animación. */
public class FavoriteButton extends Button {
    private boolean favorite;

    /** @param action acción opcional que se ejecuta después de cada clic; puede ser null */
    public FavoriteButton(Runnable action) {
        super("♡");
        getStyleClass().add("btn-icon");
        setTooltip(new Tooltip("Agregar a favoritos"));
        setOnAction(event -> {
            favorite = !favorite;
            setText(favorite ? "♥" : "♡");
            if (favorite) {
                getStyleClass().add("favorite-active");
            } else {
                getStyleClass().remove("favorite-active");
            }
            playPopAnimation();
            if (action != null) {
                action.run();
            }
        });
    }

    public boolean isFavorite() {
        return favorite;
    }

    private void playPopAnimation() {
        ScaleTransition pop = new ScaleTransition(Duration.millis(250), this);
        pop.setByX(.3);
        pop.setByY(.3);
        pop.setAutoReverse(true);
        pop.setCycleCount(2);
        pop.playFromStart();
    }
}
