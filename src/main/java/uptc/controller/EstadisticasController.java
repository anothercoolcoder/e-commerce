package uptc.controller;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import uptc.model.Compra;
import uptc.model.DetalleCompra;

/** Calcula los indicadores de ventas que muestra la pantalla de estadísticas. */
public class EstadisticasController {

    /** Suma los totales de todos los pedidos. */
    public double totalSales(List<Compra> purchases) {
        double sum = 0;
        for (Compra purchase : purchases) {
            sum += purchase.getTotal();
        }
        return sum;
    }

    /** Ticket promedio: ventas totales dividido entre el número de pedidos. */
    public double averageTicket(List<Compra> purchases) {
        return purchases.isEmpty() ? 0 : totalSales(purchases) / purchases.size();
    }

    /**
     * Cuenta las unidades vendidas por categoría, ordenadas alfabéticamente.
     * Los detalles de productos que ya no existen en el catálogo se omiten.
     */
    public Map<String, Integer> salesByCategory(List<Compra> purchases, ProductoController products) {
        Map<String, Integer> result = new TreeMap<>();
        for (Compra purchase : purchases) {
            for (DetalleCompra detail : purchase.getDetalles()) {
                if (products.exists(detail.getProductoId())) {
                    String category = products.find(detail.getProductoId()).getCategoriaId();
                    result.merge(category, detail.getCantidad(), Integer::sum);
                }
            }
        }
        return result;
    }
}
