package uptc.utils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;
import uptc.model.Producto;

/**
 * Carga las imágenes de producto desde {@code resources/images/productos} y
 * las conserva en memoria para no leer el mismo archivo dos veces.
 */
public final class ImageLoader {
    private static final String FOLDER = "/images/productos/";
    private static final Map<String, Image> CACHE = new HashMap<>();

    private ImageLoader() {
    }

    /** Devuelve la imagen del producto, o null si no tiene imagen o el archivo no existe. */
    public static Image loadProduct(Producto product) {
        if (product == null || product.getImagen() == null || product.getImagen().isBlank()) {
            return null;
        }
        String name = product.getImagen();
        if (!CACHE.containsKey(name)) {
            URL resource = ImageLoader.class.getResource(FOLDER + name);
            // El último argumento carga la imagen en segundo plano para no bloquear la pantalla.
            CACHE.put(name, resource == null ? null : new Image(resource.toExternalForm(), 196, 196, true, true, true));
        }
        return CACHE.get(name);
    }
}
