package uptc.model;

import java.time.LocalDateTime;

public class Interaccion {
    private int usuarioId;
    private int productoId;
    private TipoInteraccion tipo;
    private LocalDateTime fecha = LocalDateTime.now();
    private double peso;
    public Interaccion() { }
    public Interaccion(int usuarioId, int productoId, TipoInteraccion tipo) { this.usuarioId=usuarioId; this.productoId=productoId; this.tipo=tipo; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int v) { usuarioId=v; }
    public int getProductoId() { return productoId; }
    public void setProductoId(int v) { productoId=v; }
    public TipoInteraccion getTipo() { return tipo; }
    public void setTipo(TipoInteraccion v) { tipo=v; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime v) { fecha=v; }
    public double getPeso() { return peso; }
    public void setPeso(double v) { peso=v; }
}
