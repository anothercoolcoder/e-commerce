package uptc.service;
import java.util.List;
import uptc.dto.RecomendacionDto;
public interface RecomendacionService { List<RecomendacionDto> recomendar(int usuarioId,int limite); }
