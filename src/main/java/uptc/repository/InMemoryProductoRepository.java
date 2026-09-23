package uptc.repository;
import java.util.*;
import uptc.model.Producto;
public class InMemoryProductoRepository implements ProductoRepository { private final Map<Integer,Producto> data=new LinkedHashMap<>(); public Producto save(Producto p){data.put(p.getId(),p);return p;} public Optional<Producto> findById(int id){return Optional.ofNullable(data.get(id));} public List<Producto> findAll(){return new ArrayList<>(data.values());} public void delete(int id){data.remove(id);} }
