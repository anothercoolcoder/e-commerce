package uptc.repository;
import java.util.List;
import java.util.Optional;
import uptc.model.Producto;
public interface ProductoRepository { Producto save(Producto p); Optional<Producto> findById(int id); List<Producto> findAll(); void delete(int id); }
