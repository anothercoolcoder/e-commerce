package uptc.model;

import java.util.List;
import java.util.ArrayList;


public class Preferencia {
    private String id;
    private String usuarioId;
    private String categoriaId;
    private String marca;
    private double precioMinimo;
    private double precioMaximo;
    private List<String> etiquetas;

    public Preferencia(String id, String usuarioId, String categoriaId, String marca, double precioMinimo,
            double precioMaximo, List<String> etiquetas) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.categoriaId = categoriaId;
        this.marca = marca;
        this.precioMinimo = precioMinimo;
        this.precioMaximo = precioMaximo;
        this.etiquetas = etiquetas;
    }
    public Preferencia() {
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUsuarioId() {
        return usuarioId;
    }
    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
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
    public double getPrecioMinimo() {
        return precioMinimo;
    }
    public void setPrecioMinimo(double precioMinimo) {
        this.precioMinimo = precioMinimo;
    }
    public double getPrecioMaximo() {
        return precioMaximo;
    }
    public void setPrecioMaximo(double precioMaximo) {
        this.precioMaximo = precioMaximo;
    }
    public List<String> getEtiquetas() {
        return etiquetas;
    }
    public void setEtiquetas(List<String> etiquetas) {
        this.etiquetas = etiquetas;
    }

    
}
