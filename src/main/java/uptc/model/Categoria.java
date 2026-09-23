package uptc.model;

public class Categoria {
    private int id;
    private String nombre;
    private String descripcion;
    private boolean activa = true;
    public Categoria() { }
    public Categoria(int id, String nombre) { this.id = id; this.nombre = nombre; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
