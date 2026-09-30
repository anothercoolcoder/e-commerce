package uptc.model;

public class Producto {
    private String id;
    private String sku;
    private String nombre;
    private String descripcion;
    private double precio;
    private double stock;
    private String categoriaId;
    private String marca;
    private String etiquetas;
    private boolean activo;
    private double descuento;
    private double calificacion;
    private String imagen;

    public Producto(String id, String nombre, String descripcion, double precio, double stock, String categoriaId,
            String marca, String etiquetas, boolean activo) {
        this.id = id;
        this.sku = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoriaId = categoriaId;
        this.marca = marca;
        this.etiquetas = etiquetas;
        this.activo = activo;
    }

    /** Crea un producto con los atributos comerciales usados por el catálogo. */
    public Producto(String id, String sku, String nombre, String descripcion, double precio, double descuento,
            double stock, String categoriaId, String marca, double calificacion, String imagen, boolean activo) {
        this(id, nombre, descripcion, precio, stock, categoriaId, marca, "", activo);
        this.sku = sku;
        this.descuento = descuento;
        this.calificacion = calificacion;
        this.imagen = imagen;
    }

    public Producto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getStock() {
        return stock;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }

    public String getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(String categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getEtiquetas() {
        return etiquetas;
    }

    public void setEtiquetas(String etiquetas) {
        this.etiquetas = etiquetas;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public double getDescuento() { return descuento; }
    public void setDescuento(double descuento) { this.descuento = descuento; }
    public double getCalificacion() { return calificacion; }
    public void setCalificacion(double calificacion) { this.calificacion = calificacion; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    /** Precio que paga el cliente: el precio base menos el porcentaje de descuento. */
    public double precioFinal() {
        return precio * (1 - descuento / 100);
    }

}
