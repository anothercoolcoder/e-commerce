package uptc;

import java.nio.file.Path;
import uptc.controller.ProductoController;
import uptc.model.Producto;
import uptc.model.RolUsuario;
import uptc.model.Usuario;
import uptc.persistence.ProductoCsvDao;

/** Datos compartidos por las pruebas: un usuario de cada rol y productos de ejemplo. */
public final class DatosDePrueba {
    public static final Usuario ADMIN = new Usuario("admin", "Admin", "admin@test.com", true, RolUsuario.ADMIN);
    public static final Usuario CLIENTE = new Usuario("cliente", "Cliente", "cliente@test.com", true, RolUsuario.CLIENTE);

    private DatosDePrueba() {
    }

    /** Producto activo, sin descuento y con calificación 4. */
    public static Producto producto(String id, String nombre, String categoria, String marca, double precio, double stock) {
        return new Producto(id, "SKU-" + id, nombre, "Descripción", precio, 0, stock, categoria, marca, 4, "", true);
    }

    /** Crea un catálogo que guarda en un CSV dentro de la carpeta temporal de la prueba. */
    public static ProductoController catalogo(Path carpeta, Producto... productos) throws Exception {
        ProductoController catalogo = new ProductoController(new ProductoCsvDao(carpeta.resolve("productos.csv")), 4);
        for (Producto producto : productos) {
            catalogo.create(ADMIN, producto);
        }
        return catalogo;
    }
}
