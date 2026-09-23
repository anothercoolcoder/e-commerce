package uptc.service;

import java.util.List;
import java.util.Objects;
import uptc.exception.EntityNotFoundException;
import uptc.exception.PersistenceException;
import uptc.exception.ValidationException;
import uptc.model.Categoria;
import uptc.model.Producto;
import uptc.model.RolUsuario;
import uptc.model.Usuario;
import uptc.repository.CategoriaRepository;
import uptc.repository.ProductoRepository;
import uptc.structures.catalogo.ArbolCatalogo;

public class CatalogoServiceImpl implements CatalogoService {
    private final ProductoRepository productos; private final CategoriaRepository categorias; private final ArbolCatalogo arbol;
    public CatalogoServiceImpl(ProductoRepository p,CategoriaRepository c,ArbolCatalogo a)throws PersistenceException{productos=Objects.requireNonNull(p);categorias=Objects.requireNonNull(c);arbol=Objects.requireNonNull(a);for(Producto x:p.findAll())arbol.insertar(x);}
    private void admin(Usuario u)throws ValidationException{if(u==null||u.getRol()!=RolUsuario.ADMIN)throw new ValidationException("Solo un usuario ADMIN puede modificar el catálogo");}
    private void validar(Producto p)throws ValidationException{if(p==null||p.getId()==null||p.getId().isBlank()||p.getNombre()==null||p.getNombre().isBlank()||p.getPrecio()<0||p.getStock()<0)throw new ValidationException("Producto inválido");}
    public Producto crear(Usuario u,Producto p)throws ValidationException,PersistenceException{admin(u);validar(p);if(productos.existsById(p.getId()))throw new ValidationException("El id del producto ya existe");Producto r=productos.save(p);arbol.insertar(r);return r;}
    public Producto actualizar(Usuario u,Producto p)throws ValidationException,EntityNotFoundException,PersistenceException{admin(u);validar(p);if(!productos.existsById(p.getId()))throw new EntityNotFoundException("Producto no encontrado: "+p.getId());Producto r=productos.save(p);arbol.eliminar(p.getId());arbol.insertar(r);return r;}
    public void eliminar(Usuario u,String id)throws ValidationException,EntityNotFoundException,PersistenceException{admin(u);Producto p=productos.findById(id).orElseThrow(()->new EntityNotFoundException("Producto no encontrado: "+id));p.setActivo(false);productos.save(p);arbol.eliminar(id);arbol.insertar(p);}
    public Producto buscar(String id)throws PersistenceException{return productos.findById(id).orElse(null);} public List<Producto> listar()throws PersistenceException{return productos.findAll();} public List<Producto> rango(double min,double max){return arbol.buscarPorRangoPrecio(min,max);}
    public Categoria crearCategoria(Usuario u,Categoria c)throws ValidationException,PersistenceException{admin(u);if(c==null||c.getId()==null||c.getId().isBlank()||c.getNombre()==null||c.getNombre().isBlank())throw new ValidationException("Categoría inválida");if(categorias.existsById(c.getId()))throw new ValidationException("El id de categoría ya existe");return categorias.save(c);}
    public Categoria actualizarCategoria(Usuario u,Categoria c)throws ValidationException,EntityNotFoundException,PersistenceException{admin(u);if(c==null||!categorias.existsById(c.getId()))throw new EntityNotFoundException("Categoría no encontrada");return categorias.save(c);}
    public void eliminarCategoria(Usuario u,String id)throws ValidationException,EntityNotFoundException,PersistenceException{admin(u);if(!categorias.existsById(id))throw new EntityNotFoundException("Categoría no encontrada: "+id);categorias.deleteById(id);}
    public List<Categoria> listarCategorias()throws PersistenceException{return categorias.findAll();}
}
