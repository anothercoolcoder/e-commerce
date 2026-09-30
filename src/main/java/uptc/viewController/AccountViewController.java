package uptc.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import uptc.model.Usuario;

/** Perfil del usuario activo y cantidad de pedidos que ha realizado. */
public class AccountViewController {
    @FXML private Label name, email, orders;

    @FXML
    private void initialize() {
        Usuario user = ShopContext.currentUser();
        name.setText(user.getNombre());
        email.setText(user.getCorreo());
        orders.setText("Pedidos realizados: " + ShopContext.purchases().history(user.getId()).size());
    }

    @FXML
    private void back() {
        ViewNavigator.go("store");
    }
}
