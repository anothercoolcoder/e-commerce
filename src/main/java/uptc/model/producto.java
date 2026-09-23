package uptc.model;

import java.util.ArrayList;
import java.util.List;

public class Producto {
    private int id;
    private String nombre;
    private String descripcion;
    private double precio;
    private int stock;
    private int categoriaId;
    private String marca;
    private List<String> etiquetas = new ArrayList<>();
    private boolean activo = true;
    public Producto() { }
    public Producto(int id, String nombre, double precio, int stock, int categoriaId) { this.id=id; this.nombre=nombre; this.precio=precio; this.stock=stock; this.categoriaId=categoriaId; }
    public int getId(){return id;} public void setId(int v){id=v;}
    public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
    public String getDescripcion(){return descripcion;} public void setDescripcion(String v){descripcion=v;}
    public double getPrecio(){return precio;} public void setPrecio(double v){precio=v;}
    public int getStock(){return stock;} public void setStock(int v){stock=v;}
    public int getCategoriaId(){return categoriaId;} public void setCategoriaId(int v){categoriaId=v;}
    public String getMarca(){return marca;} public void setMarca(String v){marca=v;}
    public List<String> getEtiquetas(){return etiquetas;} public void setEtiquetas(List<String> v){etiquetas=v==null?new ArrayList<>():new ArrayList<>(v);}
    public boolean isActivo(){return activo;} public void setActivo(boolean v){activo=v;}
}
