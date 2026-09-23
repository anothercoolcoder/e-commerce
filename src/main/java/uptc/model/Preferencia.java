package uptc.model;

import java.util.HashSet;
import java.util.Set;

public class Preferencia {
    private Integer categoriaId;
    private String marca;
    private double precioMin = 0;
    private double precioMax = Double.MAX_VALUE;
    private Set<String> etiquetas = new HashSet<>();
    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer v) { categoriaId=v; }
    public String getMarca() { return marca; }
    public void setMarca(String v) { marca=v; }
    public double getPrecioMin() { return precioMin; }
    public void setPrecioMin(double v) { precioMin=v; }
    public double getPrecioMax() { return precioMax; }
    public void setPrecioMax(double v) { precioMax=v; }
    public Set<String> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(Set<String> v) { etiquetas=v == null ? new HashSet<>() : new HashSet<>(v); }
}
