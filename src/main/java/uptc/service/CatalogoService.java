package uptc.service;
import java.util.List;
import uptc.exception.EntityNotFoundException;
import uptc.exception.PersistenceException;
import uptc.exception.ValidationException;
import uptc.model.*;
public interface CatalogoService {
    Producto crear(Usuario usuario, Producto producto) throws ValidationException, PersistenceException;
    Producto actualizar(Usuario usuario, Producto producto) throws ValidationException, EntityNotFoundException, PersistenceException;
    void eliminar(Usuario usuario, String id) throws ValidationException, EntityNotFoundException, PersistenceException;
    Producto buscar(String id) throws PersistenceException;
    List<Producto> listar() throws PersistenceException;
    List<Producto> rango(double minimo, double maximo);
    Categoria crearCategoria(Usuario usuario, Categoria categoria) throws ValidationException, PersistenceException;
    Categoria actualizarCategoria(Usuario usuario, Categoria categoria) throws ValidationException, EntityNotFoundException, PersistenceException;
    void eliminarCategoria(Usuario usuario, String id) throws ValidationException, EntityNotFoundException, PersistenceException;
    List<Categoria> listarCategorias() throws PersistenceException;
}
