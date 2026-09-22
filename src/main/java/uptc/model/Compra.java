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
}
