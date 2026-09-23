package uptc.structures.catalogo;
import java.util.List;
import uptc.model.Producto;
public interface ArbolCatalogo { void insertar(Producto producto); Producto buscar(int id); void actualizar(Producto producto); Producto eliminar(int id); List<Producto> inOrder(); List<Producto> buscarPorRango(double minimo,double maximo); }
