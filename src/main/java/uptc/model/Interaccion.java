package uptc.model;

import java.time.LocalDateTime;

public class Interaccion {
    String id;
    String usuarioId;
    String productoId;
    TipoInteraccion tipo;
    LocalDateTime fecha;
    double peso;

    public Interaccion(String id, String usuarioId, String productoId, TipoInteraccion tipo, LocalDateTime fecha,
            double peso) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.productoId = productoId;
        this.tipo = tipo;
        this.fecha = fecha;
        this.peso = peso;
    }
    public Interaccion() {
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
    public String getProductoId() {
        return productoId;
    }
    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }
    public TipoInteraccion getTipo() {
        return tipo;
    }
    public void setTipo(TipoInteraccion tipo) {
        this.tipo = tipo;
    }
    public LocalDateTime getFecha() {
        return fecha;
    }
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    public double getPeso() {
        return peso;
    }
    public void setPeso(double peso) {
        this.peso = peso;
    }

    
}
