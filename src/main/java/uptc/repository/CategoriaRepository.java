package uptc.repository;
import java.util.List;
import java.util.Optional;
import uptc.model.Categoria;
public interface CategoriaRepository { Categoria save(Categoria c); Optional<Categoria> findById(int id); List<Categoria> findAll(); void delete(int id); }
