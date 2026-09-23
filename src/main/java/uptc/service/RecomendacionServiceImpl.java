package uptc.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import uptc.dto.RecomendacionDto;
import uptc.exception.PersistenceException;
import uptc.model.Interaccion;
import uptc.model.Preferencia;
import uptc.model.Producto;
import uptc.model.TipoInteraccion;
import uptc.structures.decision.ArbolDecision;

public class RecomendacionServiceImpl implements RecomendacionService {
    private final CatalogoService catalogo; private final HistorialService historial; private final ArbolDecision decisiones;
    public RecomendacionServiceImpl(CatalogoService c, HistorialService h, ArbolDecision d) { catalogo=Objects.requireNonNull(c); historial=Objects.requireNonNull(h); decisiones=Objects.requireNonNull(d); }
    public List<RecomendacionDto> recomendar(String usuarioId, int limite) throws PersistenceException {
        if (limite <= 0) return List.of();
        List<Interaccion> eventos = historial.consultar(usuarioId);
        Map<String, Double> puntuacion = new HashMap<>(); Set<String> comprados = new HashSet<>();
        for (Interaccion e : eventos) {
            double peso = e.getPeso() > 0 ? e.getPeso() : HistorialServiceImpl.peso(e.getTipo());
            puntuacion.merge(e.getProductoId(), peso, Double::sum);
            if (e.getTipo() == TipoInteraccion.COMPRA) comprados.add(e.getProductoId());
        }
        Preferencia pref = preferencia(eventos);
        decisiones.evaluar(pref);
        List<RecomendacionDto> resultado = new ArrayList<>();
        for (Producto p : catalogo.listar()) {
            if (!p.isActivo() || p.getStock() <= 0 || comprados.contains(p.getId())) continue;
            double score = puntuacion.getOrDefault(p.getId(), 0d);
            if (pref.getCategoriaId() != null && pref.getCategoriaId().equals(p.getCategoriaId())) score += 3;
            if (pref.getMarca() != null && pref.getMarca().equalsIgnoreCase(p.getMarca())) score += 2;
            if (score == 0) score = .1;
            resultado.add(new RecomendacionDto(p, score, motivo(p, pref)));
        }
        resultado.sort(Comparator.comparingDouble(RecomendacionDto::getRelevancia).reversed());
        return resultado.subList(0, Math.min(limite, resultado.size()));
    }
    private Preferencia preferencia(List<Interaccion> eventos) throws PersistenceException {
        Preferencia p = new Preferencia(); Map<String, Double> categorias = new HashMap<>(); Map<String, Double> marcas = new HashMap<>();
        for (Interaccion e : eventos) {
            Producto producto = catalogo.buscar(e.getProductoId()); if (producto == null) continue;
            double peso = e.getPeso() > 0 ? e.getPeso() : HistorialServiceImpl.peso(e.getTipo());
            categorias.merge(producto.getCategoriaId(), peso, Double::sum);
            if (producto.getMarca() != null) marcas.merge(producto.getMarca(), peso, Double::sum);
        }
        categorias.entrySet().stream().max(Map.Entry.comparingByValue()).ifPresent(x -> p.setCategoriaId(x.getKey()));
        marcas.entrySet().stream().max(Map.Entry.comparingByValue()).ifPresent(x -> p.setMarca(x.getKey()));
        return p;
    }
    private String motivo(Producto p, Preferencia pref) {
        if (pref.getCategoriaId() != null && pref.getCategoriaId().equals(p.getCategoriaId())) return "Pertenece a una categoría de interés";
        return "Producto disponible del catálogo";
    }
}
