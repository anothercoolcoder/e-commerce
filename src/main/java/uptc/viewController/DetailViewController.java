package uptc.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import uptc.model.Producto;
import uptc.model.TipoInteraccion;
import uptc.utils.Formato;
import uptc.utils.I18n;
import uptc.viewController.components.InteractionTracker;
import uptc.viewController.components.ProductEmoji;

/** Detalle de un producto y opción de agregarlo al carrito. */
public class DetailViewController {
    @FXML private Label emoji, name, description, price, stock, status;
    @FXML private Spinner<Integer> quantity;

    private Producto product;

    @FXML
    private void initialize() {
        product = ShopContext.selectedProduct();
        if (product == null) {
            return;
        }
        emoji.setText(ProductEmoji.of(product));
        name.setText(product.getNombre());
        description.setText(product.getDescripcion());
        price.setText(Formato.moneda(product.precioFinal()));
        stock.setText(I18n.format("detail.stock", (int) product.getStock()));
        int maximum = Math.max(1, (int) product.getStock());
        quantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maximum, 1));
    }

    @FXML
    private void add() {
        try {
            ShopContext.cart().add(product.getId(), quantity.getValue());
            InteractionTracker.track(product.getId(), TipoInteraccion.CARRITO);
            status.setText(I18n.text("detail.added"));
        } catch (RuntimeException e) {
            // Stock insuficiente o producto inactivo: se muestra el motivo al usuario.
            status.setText(e.getMessage());
        }
    }

    @FXML
    private void back() {
        ViewNavigator.go("store");
    }
}
