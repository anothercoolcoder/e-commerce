package uptc.service;
import java.util.List;
import uptc.model.Interaccion;
public interface HistorialService { void registrar(Interaccion interaccion); List<Interaccion> consultar(int usuarioId); }
