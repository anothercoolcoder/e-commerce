package uptc.viewController;

import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import uptc.controller.EstadisticasController;
import uptc.controller.ProductoController;
import uptc.model.Compra;
import uptc.model.Producto;
import uptc.model.Usuario;
import uptc.utils.Formato;
import uptc.utils.I18n;

/**
 * Pestaña de estadísticas del administrador. Los cálculos los hace
 * {@link EstadisticasController}; aquí solo se muestran.
 */
public class StatisticsViewController {
    private static final int TOP_PRODUCTS = 5;
    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int LOW_STOCK_ROWS = 8;

    @FXML private Label sales, orders, ticket, units;
    @FXML private BarChart<String, Number> categoryChart, customerChart;
    @FXML private VBox topProducts, lowStock;

    private final EstadisticasController statistics = new EstadisticasController();

    @FXML
    private void initialize() {
        List<Compra> purchases = ShopContext.purchases().all();
        ProductoController products = ShopContext.products();

        sales.setText(Formato.moneda(statistics.totalSales(purchases)));
        orders.setText(String.valueOf(purchases.size()));
        ticket.setText(Formato.moneda(statistics.averageTicket(purchases)));
        units.setText(String.valueOf(statistics.unitsSold(purchases)));

        showCategoryChart(statistics.salesByCategory(purchases, products));
        showCustomerChart(statistics.salesByUser(purchases));
        showTopProducts(purchases, products);
        showLowStock(products);
    }

    private void showCategoryChart(Map<String, Integer> unitsByCategory) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Integer> entry : unitsByCategory.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        categoryChart.getData().add(series);
    }

    private void showCustomerChart(Map<String, Double> salesByUser) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Double> entry : salesByUser.entrySet()) {
            series.getData().add(new XYChart.Data<>(userName(entry.getKey()), entry.getValue()));
        }
        customerChart.getData().add(series);
    }

    private void showTopProducts(List<Compra> purchases, ProductoController products) {
        Map<String, Integer> unitsByProduct = statistics.unitsByProduct(purchases);
        for (String productId : statistics.topProducts(purchases, TOP_PRODUCTS)) {
            String name = products.exists(productId) ? products.find(productId).getNombre() : "#" + productId;
            topProducts.getChildren().add(new Label(I18n.format("stats.topLine", name, unitsByProduct.get(productId))));
        }
    }

    private void showLowStock(ProductoController products) {
        List<Producto> low = statistics.lowStock(products, LOW_STOCK_THRESHOLD);
        for (int i = 0; i < low.size() && i < LOW_STOCK_ROWS; i++) {
            Producto product = low.get(i);
            Label line = new Label(I18n.format("stats.lowLine", product.getNombre(), (int) product.getStock()));
            line.getStyleClass().add("stock-low");
            lowStock.getChildren().add(line);
        }
    }

    /** Nombre del usuario para el gráfico; si el usuario ya no existe se muestra su id. */
    private String userName(String userId) {
        for (Usuario user : ShopContext.users().findAll()) {
            if (user.getId().equals(userId)) {
                return user.getNombre();
            }
        }
        return userId;
    }
}
