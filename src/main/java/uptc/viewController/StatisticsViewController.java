package uptc.viewController;

import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import uptc.controller.EstadisticasController;
import uptc.model.Compra;
import uptc.utils.Formato;

/** Indicadores de ventas y gráfico de unidades vendidas por categoría. */
public class StatisticsViewController {
    @FXML private Label sales, orders, ticket;
    @FXML private BarChart<String, Number> chart;

    @FXML
    private void initialize() {
        List<Compra> purchases = ShopContext.purchases().all();
        EstadisticasController statistics = new EstadisticasController();

        sales.setText(Formato.moneda(statistics.totalSales(purchases)));
        orders.setText(String.valueOf(purchases.size()));
        ticket.setText(Formato.moneda(statistics.averageTicket(purchases)));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Unidades vendidas");
        Map<String, Integer> unitsByCategory = statistics.salesByCategory(purchases, ShopContext.products());
        for (Map.Entry<String, Integer> entry : unitsByCategory.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        chart.getData().add(series);
    }

    @FXML
    private void back() {
        ViewNavigator.go("admin");
    }
}
