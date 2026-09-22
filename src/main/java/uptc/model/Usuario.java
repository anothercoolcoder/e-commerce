package uptc.model;

import java.util.List;
import java.util.ArrayList;

public class Usuario {
    private String id;
    private String nombre;
    private String correo;
    private boolean estado;
    private RolUsuario rol;
    private List<Preferencia> preferencias;
    public Usuario() {
    }
    public Usuario(String id, String nombre, String correo, boolean estado, RolUsuario rol) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.estado = estado;
        this.rol = rol;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getCorreo() {
        return correo;
    }
    public void setCorreo(String correo) {
        this.correo = correo;
    }
    public boolean isEstado() {
        return estado;
    }
    public void setEstado(boolean estado) {
        this.estado = estado;
    }
    public RolUsuario getRol() {
        return rol;
    }
    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    

}
