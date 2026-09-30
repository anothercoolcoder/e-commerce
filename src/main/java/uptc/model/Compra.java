package uptc.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;


public class Compra {
    private String id;
    private String usuarioId;
    private LocalDateTime fecha;
    private List<DetalleCompra> detalles;
    private double total;

    public Compra() { this.detalles = new ArrayList<>(); }
    public Compra(String id, String usuarioId, LocalDateTime fecha, List<DetalleCompra> detalles, double total) {
        this.id = id; this.usuarioId = usuarioId; this.fecha = fecha;
        this.detalles = detalles == null ? new ArrayList<>() : new ArrayList<>(detalles); this.total = total;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public List<DetalleCompra> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleCompra> detalles) { this.detalles = detalles; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}
