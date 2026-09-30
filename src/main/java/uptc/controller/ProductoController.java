package uptc.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import uptc.controller.tree.CatalogoBPlusIndex;
import uptc.exception.PersistenceException;
import uptc.exception.ProductoNoEncontradoException;
import uptc.exception.StockInsuficienteException;
import uptc.exception.ValidationException;
import uptc.model.Producto;
import uptc.model.Usuario;
import uptc.persistence.ProductoCsvDao;
import uptc.structures.catalogo.ArbolAVL;
import uptc.structures.catalogo.ArbolCatalogo;
import uptc.utils.Validaciones;

/**
 * CRUD y consultas del catálogo de productos.
 *
 * <p>Los productos se guardan en el CSV mediante el DAO y se mantienen
 * indexados en memoria con dos estructuras:</p>
 * <ul>
 *   <li>un árbol AVL ordenado por id, para la búsqueda exacta de un producto;</li>
 *   <li>un índice B+ por nombre y por precio, para buscar por prefijo y por rango.</li>
 * </ul>
 */
public class ProductoController {
    private final ProductoCsvDao dao;
    private final List<Producto> products = new ArrayList<>();
    private final ArbolCatalogo byId = new ArbolAVL();
    private final CatalogoBPlusIndex index;

    /** Carga el catálogo desde el DAO y construye los índices. */
    public ProductoController(ProductoCsvDao dao, int bPlusOrder) throws PersistenceException {
        this.dao = Objects.requireNonNull(dao);
        this.index = new CatalogoBPlusIndex(bPlusOrder);
        reload();
    }

    /** Vuelve a leer el archivo y reconstruye los índices. */
    public final void reload() throws PersistenceException {
        products.clear();
        products.addAll(dao.load());
        rebuildIndexes();
    }

    /** Crea un producto. Solo ADMIN. El id y el SKU no pueden repetirse. */
    public Producto create(Usuario admin, Producto product) throws ValidationException, PersistenceException {
        Validaciones.admin(admin);
        Validaciones.producto(product);
        if (exists(product.getId())) {
            throw new ValidationException("El ID ya existe: " + product.getId());
        }
        validateUniqueSku(product);
        products.add(product);
        byId.insertar(product);
        index.insert(product);
        dao.save(products);
        return product;
    }

    /** Actualiza un producto existente. Solo ADMIN. */
    public Producto update(Usuario admin, Producto product) throws ValidationException, PersistenceException {
        Validaciones.admin(admin);
        Validaciones.producto(product);
        Producto old = find(product.getId());
        validateUniqueSku(product);
        products.set(products.indexOf(old), product);
        // El nombre o el precio pudieron cambiar y son las claves del B+: se reconstruyen los índices.
        rebuildIndexes();
        dao.save(products);
        return product;
    }

    /** Eliminación lógica: desactiva el producto para conservar el historial de compras. Solo ADMIN. */
    public void delete(Usuario admin, String id) throws ValidationException, PersistenceException {
        Validaciones.admin(admin);
        Producto product = find(id);
        product.setActivo(false);
        dao.save(products);
    }

    /** Descuenta unidades vendidas. Lo usa el checkout, por eso no exige rol ADMIN. */
    public void reduceStock(String id, int quantity) throws PersistenceException {
        Producto product = find(id);
        if (quantity <= 0 || quantity > product.getStock()) {
            throw new StockInsuficienteException("Stock insuficiente para " + product.getNombre());
        }
        product.setStock(product.getStock() - quantity);
        dao.save(products);
    }

    /** Busca un producto por id en el árbol AVL. */
    public Producto find(String id) {
        return byId.buscar(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado: " + id));
    }

    /** Indica si existe un producto con ese id. */
    public boolean exists(String id) {
        return byId.buscar(id).isPresent();
    }

    /** Devuelve una copia de la lista de productos. */
    public List<Producto> findAll() {
        return List.copyOf(products);
    }

    /** Busca por prefijo del nombre mediante el índice B+. */
    public List<Producto> searchPrefix(String prefix) {
        return index.findByNamePrefix(prefix);
    }

    /** Busca por rango de precio mediante el índice B+. */
    public List<Producto> searchPrice(double min, double max) {
        return index.findByPriceRange(min, max);
    }

    /**
     * Búsqueda de la tienda: productos activos cuyo nombre comienza por el texto
     * (índice B+), filtrados por categoría, marca y disponibilidad. Un filtro en
     * null no se aplica.
     */
    public List<Producto> filter(String text, String category, String brand, boolean onlyAvailable) {
        List<Producto> result = new ArrayList<>();
        for (Producto product : index.findByNamePrefix(text)) {
            boolean categoryMatches = category == null || category.equals(product.getCategoriaId());
            boolean brandMatches = brand == null || brand.equalsIgnoreCase(product.getMarca());
            boolean availabilityMatches = !onlyAvailable || product.getStock() > 0;
            if (product.isActivo() && categoryMatches && brandMatches && availabilityMatches) {
                result.add(product);
            }
        }
        return result;
    }

    /** Devuelve las categorías que aparecen en el catálogo, sin repetir. */
    public List<String> categoryNames() {
        List<String> names = new ArrayList<>();
        for (Producto product : products) {
            String category = product.getCategoriaId();
            if (!Validaciones.vacio(category) && !names.contains(category)) {
                names.add(category);
            }
        }
        return names;
    }

    private void validateUniqueSku(Producto product) throws ValidationException {
        for (Producto other : products) {
            boolean sameProduct = other.getId().equals(product.getId());
            if (!sameProduct && other.getSku().equalsIgnoreCase(product.getSku())) {
                throw new ValidationException("El SKU ya existe: " + product.getSku());
            }
        }
    }

    private void rebuildIndexes() {
        byId.vaciar();
        index.clear();
        for (Producto product : products) {
            byId.insertar(product);
            index.insert(product);
        }
    }
}
