package uptc.viewController;

import java.util.ArrayList;
import java.util.List;
import uptc.controller.CarritoController;
import uptc.controller.CategoriaController;
import uptc.controller.CompraController;
import uptc.controller.ProductoController;
import uptc.controller.RecomendacionController;
import uptc.controller.UsuarioController;
import uptc.controller.tree.DecisionTree;
import uptc.model.Categoria;
import uptc.model.Interaccion;
import uptc.model.Producto;
import uptc.model.RolUsuario;
import uptc.model.Usuario;
import uptc.persistence.PersistenceManager;

/**
 * Estado compartido de la sesión. Crea una sola vez los controladores de
 * negocio para que todas las pantallas usen el mismo catálogo, el mismo
 * carrito y el mismo usuario.
 */
public final class ShopContext {
    private static final int B_PLUS_ORDER = 4;

    private static ProductoController products;
    private static CategoriaController categories;
    private static CarritoController cart;
    private static UsuarioController users;
    private static CompraController purchases;
    private static RecomendacionController recommendations;
    private static final List<Interaccion> interactions = new ArrayList<>();
    private static Usuario currentUser;
    private static Producto selectedProduct;

    private ShopContext() {
    }

    /** Inicializa los controladores. Llamarlo varias veces no tiene efecto. */
    public static synchronized void initialize() {
        if (products != null) {
            return;
        }
        try {
            PersistenceManager persistence = PersistenceManager.getInstance();
            persistence.copyMissingSeedFiles();
            products = new ProductoController(persistence.productos(), B_PLUS_ORDER);
            categories = new CategoriaController(catalogCategories());
            users = new UsuarioController(persistence.usuarios());
            purchases = new CompraController(products, persistence.compras());
            cart = new CarritoController(products);
            recommendations = new RecomendacionController(products, purchases, new DecisionTree(), interactions);
            if (users.findAll().isEmpty()) {
                users.register("admin-demo", "Administrador", "admin@shoptree.local", "admin", RolUsuario.ADMIN);
                users.register("cliente-demo", "Cliente Demo", "cliente@shoptree.local", "cliente", RolUsuario.CLIENTE);
            }
            currentUser = users.findAll().get(0);
        } catch (Exception exception) {
            throw new IllegalStateException("No fue posible iniciar la tienda", exception);
        }
    }

    /** Las categorías iniciales son las que aparecen en los productos del catálogo. */
    private static List<Categoria> catalogCategories() {
        List<Categoria> result = new ArrayList<>();
        for (String name : products.categoryNames()) {
            result.add(new Categoria(name, name, "Categoría del catálogo"));
        }
        return result;
    }

    public static ProductoController products() { initialize(); return products; }
    public static CategoriaController categories() { initialize(); return categories; }
    public static CarritoController cart() { initialize(); return cart; }
    public static UsuarioController users() { initialize(); return users; }
    public static CompraController purchases() { initialize(); return purchases; }
    public static RecomendacionController recommendations() { initialize(); return recommendations; }
    public static Usuario currentUser() { initialize(); return currentUser; }
    public static void setCurrentUser(Usuario user) { currentUser = user; }
    public static List<Interaccion> interactions() { return interactions; }
    public static Producto selectedProduct() { return selectedProduct; }
    public static void setSelectedProduct(Producto product) { selectedProduct = product; }
}
