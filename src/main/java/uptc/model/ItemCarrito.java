package uptc.model;

/** Línea de un carrito de compra. */
public class ItemCarrito {
    private Producto producto;
    private int cantidad;

    public ItemCarrito() { }
    public ItemCarrito(Producto producto, int cantidad) { this.producto = producto; this.cantidad = cantidad; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    /** Subtotal de la línea: precio final (con descuento) por la cantidad. */
    public double getSubtotal() { return producto == null ? 0 : producto.precioFinal() * cantidad; }
}
