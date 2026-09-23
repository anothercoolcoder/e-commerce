package uptc.service;
import java.util.*;
import uptc.exception.*; import uptc.model.*; import uptc.repository.*; import uptc.structures.catalogo.*;
public class CatalogoServiceImpl implements CatalogoService {
 private final ProductoRepository productos; private final CategoriaRepository categorias; private final ArbolCatalogo arbol;
 public CatalogoServiceImpl(ProductoRepository p,CategoriaRepository c,ArbolCatalogo a){productos=Objects.requireNonNull(p);categorias=Objects.requireNonNull(c);arbol=Objects.requireNonNull(a);for(Producto x:p.findAll())arbol.insertar(x);}
 private void admin(Usuario u){if(u==null||u.getRol()!=RolUsuario.ADMIN)throw new ValidationException("Solo un usuario ADMIN puede modificar el catálogo");}
 private void validar(Producto p){if(p==null||p.getId()<=0||p.getNombre()==null||p.getNombre().isBlank()||p.getPrecio()<0||p.getStock()<0)throw new ValidationException("Producto inválido");}
 public Producto crear(Usuario u,Producto p){admin(u);validar(p);if(productos.findById(p.getId()).isPresent())throw new ValidationException("El id del producto ya existe");Producto r=productos.save(p);arbol.insertar(r);return r;}
 public Producto actualizar(Usuario u,Producto p){admin(u);validar(p);if(productos.findById(p.getId()).isEmpty())throw new EntityNotFoundException("Producto no encontrado: "+p.getId());Producto r=productos.save(p);arbol.actualizar(r);return r;}
 public void eliminar(Usuario u,int id){admin(u);Producto p=productos.findById(id).orElseThrow(()->new EntityNotFoundException("Producto no encontrado: "+id));p.setActivo(false);productos.save(p);arbol.actualizar(p);}
 public Producto buscar(int id){return productos.findById(id).orElse(null);} public List<Producto> listar(){return productos.findAll();} public List<Producto> rango(double min,double max){return arbol.buscarPorRango(min,max);}
 public Categoria crearCategoria(Usuario u,Categoria c){admin(u);if(c==null||c.getId()<=0||c.getNombre()==null||c.getNombre().isBlank())throw new ValidationException("Categoría inválida");if(categorias.findById(c.getId()).isPresent())throw new ValidationException("El id de categoría ya existe");return categorias.save(c);}
 public Categoria actualizarCategoria(Usuario u,Categoria c){admin(u);if(categorias.findById(c.getId()).isEmpty())throw new EntityNotFoundException("Categoría no encontrada: "+c.getId());return categorias.save(c);}
 public void eliminarCategoria(Usuario u,int id){admin(u);Categoria c=categorias.findById(id).orElseThrow(()->new EntityNotFoundException("Categoría no encontrada: "+id));c.setActiva(false);categorias.save(c);}
 public List<Categoria> listarCategorias(){return categorias.findAll();}
}
