package uptc.dto;

import java.util.List;

public class ProductoDto {

    private String id;
    private String nombre;
    private String descripcion;
    private double precio;
    private int stock;
    private String categoriaId;
    private String marca;
    private List<String> etiquetas;
    private boolean activo;

    public ProductoDto() {
    }

    public ProductoDto(String id, String nombre, String descripcion, double precio, int stock, String categoriaId, String marca, List<String> etiquetas, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoriaId = categoriaId;
        this.marca = marca;
        this.etiquetas = etiquetas;
        this.activo = activo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getCategoriaId() { return categoriaId; }
    public void setCategoriaId(String categoriaId) { this.categoriaId = categoriaId; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public List<String> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<String> etiquetas) { this.etiquetas = etiquetas; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}