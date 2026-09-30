package uptc.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import uptc.exception.PersistenceException;
import uptc.model.Compra;
import uptc.utils.Formato;

/** Checkout simulado: pide la dirección y confirma la compra. */
public class CheckoutViewController {
    @FXML private TextField address;
    @FXML private Label status;

    @FXML
    private void pay() {
        if (address.getText().isBlank()) {
            status.setText("La dirección es obligatoria");
            return;
        }
        try {
            Compra purchase = ShopContext.purchases().checkout(ShopContext.currentUser().getId(), ShopContext.cart());
            status.setText("Compra confirmada por " + Formato.moneda(purchase.getTotal()) + ". Pedido " + purchase.getId());
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
