package uptc.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import uptc.exception.PersistenceException;
import uptc.model.Compra;
import uptc.utils.Formato;
import uptc.utils.I18n;

/** Checkout simulado: pide la dirección y confirma la compra. */
public class CheckoutViewController {
    @FXML private TextField address;
    @FXML private Label status;

    @FXML
    private void pay() {
        if (address.getText().isBlank()) {
            status.setText(I18n.text("checkout.addressRequired"));
            return;
        }
        try {
            Compra purchase = ShopContext.purchases().checkout(ShopContext.currentUser().getId(), ShopContext.cart());
            status.setText(I18n.format("checkout.done", Formato.moneda(purchase.getTotal()), purchase.getId()));
        } catch (RuntimeException | PersistenceException e) {
            // Carrito vacío, stock insuficiente o error al guardar: se muestra el motivo al usuario.
            status.setText(e.getMessage());
        }
    }

    @FXML
    private void back() {
        ViewNavigator.go("cart");
    }
}
