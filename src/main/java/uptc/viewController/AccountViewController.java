package uptc.viewController;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import uptc.controller.EstadisticasController;
import uptc.model.Compra;
import uptc.model.Usuario;
import uptc.utils.Formato;
import uptc.utils.I18n;
import uptc.viewController.components.PurchaseList;

/** Perfil del usuario activo e historial detallado de sus compras. */
public class AccountViewController {
    @FXML private Label name, email, summary;
    @FXML private ScrollPane historyPane;

    @FXML
    private void initialize() {
        Usuario user = ShopContext.currentUser();
        List<Compra> purchases = ShopContext.purchases().history(user.getId());

        name.setText(user.getNombre());
        email.setText(user.getCorreo());
        double spent = new EstadisticasController().totalSales(purchases);
        summary.setText(I18n.format("history.summary", purchases.size(), Formato.moneda(spent)));

        PurchaseList history = new PurchaseList(ShopContext.products());
        history.show(purchases);
        historyPane.setContent(history);
    }

    @FXML
    private void back() {
        ViewNavigator.go("store");
    }
}
