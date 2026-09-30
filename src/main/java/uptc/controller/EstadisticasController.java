package uptc.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import uptc.model.Compra;
import uptc.model.DetalleCompra;
import uptc.model.Producto;

/** Calcula los indicadores que muestra la pestaña de estadísticas del administrador. */
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

    /** Total de unidades vendidas entre todos los pedidos. */
    public int unitsSold(List<Compra> purchases) {
        int units = 0;
        for (Compra purchase : purchases) {
            for (DetalleCompra detail : purchase.getDetalles()) {
                units += detail.getCantidad();
            }
        }
        return units;
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

    /** Suma lo que ha comprado cada usuario (id del usuario → total de sus pedidos). */
    public Map<String, Double> salesByUser(List<Compra> purchases) {
        Map<String, Double> result = new TreeMap<>();
        for (Compra purchase : purchases) {
            result.merge(purchase.getUsuarioId(), purchase.getTotal(), Double::sum);
        }
        return result;
    }

    /** Cuenta las unidades vendidas de cada producto (id del producto → unidades). */
    public Map<String, Integer> unitsByProduct(List<Compra> purchases) {
        Map<String, Integer> result = new TreeMap<>();
        for (Compra purchase : purchases) {
            for (DetalleCompra detail : purchase.getDetalles()) {
                result.merge(detail.getProductoId(), detail.getCantidad(), Integer::sum);
            }
        }
        return result;
    }

    /** Devuelve los ids de los productos más vendidos, del más vendido al menos vendido. */
    public List<String> topProducts(List<Compra> purchases, int limit) {
        Map<String, Integer> units = unitsByProduct(purchases);
        List<String> ids = new ArrayList<>(units.keySet());
        ids.sort((a, b) -> units.get(b) - units.get(a));
        return ids.subList(0, Math.min(Math.max(limit, 0), ids.size()));
    }

    /** Productos activos cuyo stock es menor o igual al umbral: hay que reponerlos. */
    public List<Producto> lowStock(ProductoController products, int threshold) {
        List<Producto> result = new ArrayList<>();
        for (Producto product : products.findAll()) {
            if (product.isActivo() && product.getStock() <= threshold) {
                result.add(product);
            }
        }
        result.sort((a, b) -> Double.compare(a.getStock(), b.getStock()));
        return result;
    }
}
