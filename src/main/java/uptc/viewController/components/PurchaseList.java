package uptc.viewController.components;

import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import uptc.controller.ProductoController;
import uptc.model.Compra;
import uptc.model.DetalleCompra;
import uptc.utils.Formato;
import uptc.utils.I18n;

/**
 * Historial de compras detallado: por cada pedido muestra la fecha, el total y
 * cada producto comprado con su cantidad. Lo usan "Mi cuenta" y el panel de
 * administración.
 */
public class PurchaseList extends VBox {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ProductoController products;

    public PurchaseList(ProductoController products) {
        this.products = products;
        setSpacing(12);
    }

    /** Reemplaza el contenido por los pedidos recibidos, del más reciente al más antiguo. */
    public void show(List<Compra> purchases) {
        getChildren().clear();
        if (purchases.isEmpty()) {
            Label empty = new Label(I18n.text("history.empty"));
            empty.getStyleClass().add("empty");
            getChildren().add(empty);
            return;
        }
        for (int i = purchases.size() - 1; i >= 0; i--) {
            getChildren().add(createOrder(purchases.get(i)));
        }
    }

    private VBox createOrder(Compra purchase) {
        String date = purchase.getFecha() == null ? "" : purchase.getFecha().format(DATE);
        Label header = new Label(I18n.format("history.order", shortId(purchase.getId()), date,
                Formato.moneda(purchase.getTotal())));
        header.getStyleClass().add("section-title");

        VBox order = new VBox(4, header);
        order.getStyleClass().add("order-box");
        for (DetalleCompra detail : purchase.getDetalles()) {
            order.getChildren().add(new Label(I18n.format("history.line", detail.getCantidad(),
                    productName(detail.getProductoId()), Formato.moneda(detail.getPrecioUnitario()))));
        }
        return order;
    }

    /** Nombre del producto; si ya no está en el catálogo se muestra su id. */
    private String productName(String productId) {
        return products.exists(productId) ? products.find(productId).getNombre() : "#" + productId;
    }

    /** Los pedidos nuevos tienen un id largo (UUID): se muestran solo sus primeros 8 caracteres. */
    private String shortId(String id) {
        return id != null && id.length() > 8 ? id.substring(0, 8) : id;
    }
}
