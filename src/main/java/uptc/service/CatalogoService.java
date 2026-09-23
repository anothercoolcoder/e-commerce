package uptc.service;
import java.util.List;
import uptc.model.*;
public interface CatalogoService { Producto crear(Usuario usuario,Producto producto); Producto actualizar(Usuario usuario,Producto producto); void eliminar(Usuario usuario,int id); Producto buscar(int id); List<Producto> listar(); List<Producto> rango(double minimo,double maximo); Categoria crearCategoria(Usuario usuario,Categoria categoria); Categoria actualizarCategoria(Usuario usuario,Categoria categoria); void eliminarCategoria(Usuario usuario,int id); List<Categoria> listarCategorias(); }
