package uptc.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import uptc.model.Producto;

public class InMemoryProductoRepository implements ProductoRepository {
    private final Map<String, Producto> data = new LinkedHashMap<>();

    public Producto save(Producto producto) {
        data.put(producto.getId(), producto);
        return producto;
    }

    public Optional<Producto> findById(String id) {
        return Optional.ofNullable(data.get(id));
    }

    public List<Producto> findAll() {
        return new ArrayList<>(data.values());
    }

    public void deleteById(String id) {
        data.remove(id);
    }

    public boolean existsById(String id) {
        return data.containsKey(id);
    }
}
