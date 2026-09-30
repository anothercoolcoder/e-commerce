package uptc.controller.tree;

/**
 * Datos del usuario que el árbol de decisión evalúa.
 *
 * @param categoria      categoría favorita según su historial; vacía si no tiene historial
 * @param precioPromedio precio promedio de los productos de su historial
 * @param marca          marca preferida; vacía si ninguna marca domina su historial
 * @param compras        cantidad de pedidos que ha realizado
 */
public record DecisionContext(String categoria, double precioPromedio, String marca, int compras) {
    public DecisionContext {
        categoria = categoria == null ? "" : categoria;
        marca = marca == null ? "" : marca;
    }
}
