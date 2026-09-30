package uptc.controller.tree;

import java.util.List;
import java.util.Locale;
import uptc.model.Producto;

/**
 * Índices B+ del catálogo: uno ordenado por nombre (búsqueda por prefijo) y
 * otro ordenado por precio (búsqueda por rango).
 *
 * <p>Las dos búsquedas se resuelven como un rango sobre las hojas enlazadas del
 * árbol B+, sin recorrer todo el catálogo.</p>
 */
public class CatalogoBPlusIndex {
    /** Carácter mayor que cualquier otro: sirve como límite superior de un rango de texto. */
    private static final String ULTIMO_CARACTER = String.valueOf(Character.MAX_VALUE);

    private final int order;
    private BPlusTree<String, Producto> byName;
    private BPlusTree<PriceKey, Producto> byPrice;

    /** Crea los dos índices con el mismo orden B+. */
    public CatalogoBPlusIndex(int order) {
        this.order = order;
        clear();
    }

    /** Agrega un producto a los dos índices. */
    public void insert(Producto producto) {
        if (producto == null || producto.getId() == null || producto.getNombre() == null) {
            throw new IllegalArgumentException("Producto, id y nombre obligatorios");
        }
        byName.insert(nameKey(producto), producto);
        byPrice.insert(priceKey(producto), producto);
    }

    /** Quita un producto de los dos índices. Devuelve false si no estaba. */
    public boolean delete(Producto producto) {
        if (producto == null || producto.getId() == null || producto.getNombre() == null) {
            return false;
        }
        boolean removed = byName.delete(nameKey(producto)) != null;
        byPrice.delete(priceKey(producto));
        return removed;
    }

    /**
     * Busca los productos cuyo nombre comienza con el prefijo, sin distinguir
     * mayúsculas. Todas las claves que empiezan por "aud" están entre "aud" y
     * "aud" seguido del último carácter posible, así que es un rango del B+.
     */
    public List<Producto> findByNamePrefix(String prefix) {
        String normalized = prefix == null ? "" : prefix.trim().toLowerCase(Locale.ROOT);
        return byName.range(normalized, normalized + ULTIMO_CARACTER);
    }

    /** Busca productos cuyo precio está entre mínimo y máximo, inclusive. */
    public List<Producto> findByPriceRange(double minimum, double maximum) {
        return byPrice.range(new PriceKey(minimum, ""), new PriceKey(maximum, ULTIMO_CARACTER));
    }

    /** Devuelve todos los productos ordenados por nombre. */
    public List<Producto> all() {
        return byName.values();
    }

    /** Vacía los dos índices. */
    public void clear() {
        byName = new BPlusTree<>(order);
        byPrice = new BPlusTree<>(order);
    }

    /** Devuelve el orden utilizado por los índices. */
    public int getOrder() {
        return order;
    }

    /** El id al final hace única la clave cuando dos productos se llaman igual. */
    private String nameKey(Producto product) {
        return product.getNombre().toLowerCase(Locale.ROOT) + "|" + product.getId();
    }

    private PriceKey priceKey(Producto product) {
        return new PriceKey(product.getPrecio(), product.getId());
    }
}
