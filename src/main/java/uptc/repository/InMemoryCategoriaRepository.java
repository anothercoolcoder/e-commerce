package uptc.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import uptc.model.Categoria;

public class InMemoryCategoriaRepository implements CategoriaRepository {
    private final Map<String, Categoria> data = new LinkedHashMap<>();

    public Categoria save(Categoria categoria) {
        data.put(categoria.getId(), categoria);
        return categoria;
    }

    public Optional<Categoria> findById(String id) {
        return Optional.ofNullable(data.get(id));
    }

    public List<Categoria> findAll() {
        return new ArrayList<>(data.values());
    }

    public void deleteById(String id) {
        data.remove(id);
    }

    public boolean existsById(String id) {
        return data.containsKey(id);
    }
}
