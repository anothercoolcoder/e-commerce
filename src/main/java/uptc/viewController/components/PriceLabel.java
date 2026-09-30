package uptc.viewController.components;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import uptc.utils.Formato;

/** Precio de un producto. Si tiene descuento muestra el precio anterior tachado y el ahorro. */
public class PriceLabel extends VBox {

    public PriceLabel(double price, double discount) {
        setSpacing(2);
        double finalPrice = price * (1 - discount / 100);

        Label current = new Label(Formato.moneda(finalPrice));
        current.getStyleClass().add("price");
        HBox line = new HBox(8, current);
        getChildren().add(line);

        if (discount > 0) {
            Label old = new Label(Formato.moneda(price));
            old.getStyleClass().add("price-old");
            line.getChildren().add(old);

            Label saved = new Label("Ahorras " + Formato.moneda(price - finalPrice));
            saved.getStyleClass().add("stock-ok");
            getChildren().add(saved);
        }
    }
}
