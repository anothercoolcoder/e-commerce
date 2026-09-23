package uptc.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import uptc.model.Interaccion;
import uptc.model.TipoInteraccion;

public class HistorialServiceImpl implements HistorialService {
    private final List<Interaccion> historial = new ArrayList<>();
    public void registrar(Interaccion i) {
        if (i == null || i.getTipo() == null) throw new IllegalArgumentException("La interacción y su tipo son obligatorios");
        if (i.getPeso() <= 0) i.setPeso(peso(i.getTipo()));
        historial.add(i);
    }
    public List<Interaccion> consultar(String id) {
        return historial.stream().filter(i -> Objects.equals(i.getUsuarioId(), id)).toList();
    }
    public static double peso(TipoInteraccion t) {
        return switch (t) { case VISTA -> 1; case BUSQUEDA -> 2; case CLIC -> 3; case FAVORITO -> 5; case CARRITO -> 7; case COMPRA -> 10; };
    }
}
