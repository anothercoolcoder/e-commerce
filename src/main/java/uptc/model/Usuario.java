package uptc.model;

public class Usuario {
    private int id;
    private String nombre;
    private String correo;
    private boolean activo = true;
    private RolUsuario rol = RolUsuario.CLIENTE;
    public Usuario() { }
    public Usuario(int id, String nombre, RolUsuario rol) { this.id = id; this.nombre = nombre; this.rol = rol; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }
}
