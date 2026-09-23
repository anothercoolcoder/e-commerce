package uptc.repository;
import java.util.*;
import uptc.model.Categoria;
public class InMemoryCategoriaRepository implements CategoriaRepository { private final Map<Integer,Categoria> data=new LinkedHashMap<>(); public Categoria save(Categoria c){data.put(c.getId(),c);return c;} public Optional<Categoria> findById(int id){return Optional.ofNullable(data.get(id));} public List<Categoria> findAll(){return new ArrayList<>(data.values());} public void delete(int id){data.remove(id);} }
