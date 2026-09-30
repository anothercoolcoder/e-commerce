package uptc.viewController;

import java.beans.PropertyChangeListener;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import uptc.controller.CarritoController;
import uptc.model.ItemCarrito;
import uptc.utils.Formato;

/** Pantalla del carrito: líneas del pedido y resumen de totales. */
public class CartViewController {
    @FXML private VBox lines;
    @FXML private Label subtotal, tax, shipping, total, empty;

    /** Observer del carrito: vuelve a dibujar cada vez que el carrito cambia. */
    private final PropertyChangeListener cartListener = event -> render();

    @FXML
    private void initialize() {
        ShopContext.cart().addListener(cartListener);
        render();
    }

    private void render() {
        CarritoController cart = ShopContext.cart();
        List<ItemCarrito> items = cart.getItems();

        lines.getChildren().clear();
        for (ItemCarrito item : items) {
            lines.getChildren().add(createLine(item));
        }
        empty.setVisible(items.isEmpty());

        subtotal.setText(Formato.moneda(cart.subtotal()));
        tax.setText(Formato.moneda(cart.tax()));
        shipping.setText(Formato.moneda(items.isEmpty() ? 0 : CarritoController.COSTO_ENVIO));
        total.setText(Formato.moneda(cart.total()));
    }

    private HBox createLine(ItemCarrito item) {
        Label text = new Label(item.getProducto().getNombre() + " × " + item.getCantidad());
        Label price = new Label(Formato.moneda(item.getSubtotal()));
        Button remove = new Button("Eliminar");
        remove.setOnAction(event -> ShopContext.cart().remove(item.getProducto().getId()));
        return new HBox(12, text, price, remove);
    }

    @FXML
    private void checkout() {
        leaveTo("checkout");
    }

    @FXML
    private void back() {
        leaveTo("store");
    }

    /** Al salir de la pantalla se retira el listener para que el carrito no conserve vistas viejas. */
    private void leaveTo(String view) {
        ShopContext.cart().removeListener(cartListener);
        ViewNavigator.go(view);
    }
}
