package uptc.viewController.components;

import javafx.scene.control.Label;

/** Etiqueta pequeña sobre la imagen del producto (descuento, agotado, últimas unidades). */
public class Badge extends Label {

    public Badge(String text, String styleClass) {
        super(text);
        getStyleClass().addAll("badge", styleClass);
    }
}
