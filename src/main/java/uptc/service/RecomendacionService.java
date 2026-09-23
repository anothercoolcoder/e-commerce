package uptc.service;
import java.util.List;
import uptc.dto.RecomendacionDto;
import uptc.exception.PersistenceException;
public interface RecomendacionService { List<RecomendacionDto> recomendar(String usuarioId,int limite) throws PersistenceException; }
