package uptc.controller;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import uptc.exception.ProductoNoEncontradoException;
import uptc.exception.StockInsuficienteException;
import uptc.model.ItemCarrito;
import uptc.model.Producto;

/**
 * Carrito de compras. Valida existencia, estado y stock de cada producto y
 * avisa a la interfaz cada vez que cambia (patrón Observer).
 */
public class CarritoController {
    /** IVA aplicado al subtotal. */
    public static final double TASA_IVA = 0.19;
    /** Costo fijo de envío de un pedido. */
    public static final double COSTO_ENVIO = 12_000;

    private final ProductoController products;
    private final List<ItemCarrito> items = new ArrayList<>();
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public CarritoController(ProductoController products) {
        this.products = Objects.requireNonNull(products);
    }

    /** Agrega unidades de un producto. Si ya estaba en el carrito, suma la cantidad. */
    public void add(String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        Producto product = products.find(productId);
        if (!product.isActivo()) {
            throw new ProductoNoEncontradoException("El producto está inactivo");
        }
        ItemCarrito item = findItem(productId);
        int desired = quantity + (item == null ? 0 : item.getCantidad());
        if (desired > product.getStock()) {
            throw new StockInsuficienteException("Stock insuficiente para " + product.getNombre());
        }
        if (item == null) {
            items.add(new ItemCarrito(product, quantity));
        } else {
            item.setCantidad(desired);
        }
        notifyChange();
    }

    /** Cambia la cantidad de una línea. Cero elimina la línea. */
    public void setQuantity(String productId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        if (quantity == 0) {
            remove(productId);
            return;
        }
        ItemCarrito item = findItem(productId);
        if (item == null) {
            throw new ProductoNoEncontradoException("El producto no está en el carrito");
        }
        if (quantity > item.getProducto().getStock()) {
            throw new StockInsuficienteException("Cantidad no disponible");
        }
        item.setCantidad(quantity);
        notifyChange();
    }

    /** Elimina la línea de un producto. */
    public void remove(String productId) {
        items.remove(findItem(productId));
        notifyChange();
    }

    /** Vacía el carrito. */
    public void clear() {
        items.clear();
        notifyChange();
    }

    /** Devuelve una copia de las líneas del carrito. */
    public List<ItemCarrito> getItems() {
        return List.copyOf(items);
    }

    /** Suma de los subtotales de cada línea (precio con descuento por cantidad). */
    public double subtotal() {
        double sum = 0;
        for (ItemCarrito item : items) {
            sum += item.getSubtotal();
        }
        return sum;
    }

    /** Impuesto (IVA) sobre el subtotal. */
    public double tax() {
        return subtotal() * TASA_IVA;
    }

    /** Total a pagar: subtotal + IVA + envío. Un carrito vacío no paga envío. */
    public double total() {
        return items.isEmpty() ? 0 : subtotal() + tax() + COSTO_ENVIO;
    }

    public void addListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }

    private ItemCarrito findItem(String productId) {
        for (ItemCarrito item : items) {
            if (item.getProducto().getId().equals(productId)) {
                return item;
            }
        }
        return null;
    }

    private void notifyChange() {
        changes.firePropertyChange("items", null, getItems());
    }
}
