package uptc.viewController.components;

import java.util.List;
import java.util.Map;
import javafx.scene.control.Label;
import uptc.model.Producto;

/**
 * Ilustración de un producto: un emoji grande según su categoría. Sustituye a
 * las imágenes con texto; el nombre del producto ya se muestra en la tarjeta.
 */
public class ProductEmoji extends Label {
    private static final String DEFAULT = "🛍";
    /** Emojis que ofrece el formulario de productos (también se puede escribir cualquier otro). */
    public static final List<String> OPTIONS = List.of(
            "🎧", "📱", "💻", "🖥", "⌚", "📷", "🎮", "🔌", "🏠", "🛋", "💡", "🍳", "☕", "👔", "👗", "👟",
            "⚽", "🚲", "🧸", "🎲", "📚", "💄", "🧴", "🐾", "🎁", "🛍");
    private static final Map<String, String> BY_CATEGORY = Map.ofEntries(
            Map.entry("Tecnología", "🎧"),
            Map.entry("Celulares", "📱"),
            Map.entry("Computadores", "💻"),
            Map.entry("Hogar", "🏠"),
            Map.entry("Cocina", "🍳"),
            Map.entry("Moda hombre", "👔"),
            Map.entry("Moda mujer", "👗"),
            Map.entry("Deportes", "⚽"),
            Map.entry("Juguetes", "🧸"),
            Map.entry("Libros", "📚"),
            Map.entry("Belleza", "💄"),
            Map.entry("Mascotas", "🐾"));

    public ProductEmoji(Producto product) {
        super(of(product));
        getStyleClass().add("product-emoji");
    }

    /**
     * Emoji del producto: el que le asignó el administrador o, si no tiene, el
     * de su categoría (una bolsa de compras si la categoría tampoco tiene uno).
     */
    public static String of(Producto product) {
        if (product.getEmoji() != null && !product.getEmoji().isBlank()) {
            return product.getEmoji();
        }
        String category = product.getCategoriaId() == null ? "" : product.getCategoriaId();
        return BY_CATEGORY.getOrDefault(category, DEFAULT);
    }
}
