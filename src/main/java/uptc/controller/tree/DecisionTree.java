package uptc.controller.tree;

import java.util.Objects;
import uptc.utils.I18n;

/**
 * Árbol de decisión manual que clasifica las preferencias de un usuario.
 * Cada nodo interno hace una pregunta de sí/no y cada hoja es un perfil.
 *
 * <pre>
 * ¿Tiene historial?
 * ├─ no → EXPLORADOR_NUEVO
 * └─ sí → ¿Tiene una marca preferida?
 *         ├─ sí → MARCA_PREFERIDA
 *         └─ no → ¿Ha hecho 5 compras o más?
 *                 ├─ sí → COMPRADOR_FRECUENTE
 *                 └─ no → ¿Su precio promedio supera $500.000?
 *                         ├─ sí → CATEGORIA_PREMIUM
 *                         └─ no → CATEGORIA_ECONOMICA
 * </pre>
 *
 * <p>Cada nodo guarda la clave de su pregunta en los archivos de idioma
 * ({@code tree.q.*}), para que el camino se pueda mostrar en español o inglés.</p>
 */
public class DecisionTree {
    /** Precio promedio a partir del cual se considera que el usuario busca gama alta. */
    public static final double PRECIO_PREMIUM = 500_000;
    /** Cantidad de compras a partir de la cual el usuario es comprador frecuente. */
    public static final int COMPRAS_FRECUENTE = 5;

    private final DecisionNode root;

    /** Construye el árbol con las reglas del recomendador. */
    public DecisionTree() {
        DecisionNode price = DecisionNode.question("tree.q.price",
                context -> context.precioPromedio() > PRECIO_PREMIUM,
                DecisionNode.leaf(RecommendationProfile.CATEGORIA_PREMIUM),
                DecisionNode.leaf(RecommendationProfile.CATEGORIA_ECONOMICA));
        DecisionNode purchases = DecisionNode.question("tree.q.purchases",
                context -> context.compras() >= COMPRAS_FRECUENTE,
                DecisionNode.leaf(RecommendationProfile.COMPRADOR_FRECUENTE),
                price);
        DecisionNode brand = DecisionNode.question("tree.q.brand",
                context -> !context.marca().isBlank(),
                DecisionNode.leaf(RecommendationProfile.MARCA_PREFERIDA),
                purchases);
        root = DecisionNode.question("tree.q.history",
                context -> !context.categoria().isBlank(),
                brand,
                DecisionNode.leaf(RecommendationProfile.EXPLORADOR_NUEVO));
    }

    /** Recorre el árbol desde la raíz y devuelve el perfil de la hoja alcanzada. */
    public RecommendationProfile classify(DecisionContext context) {
        Objects.requireNonNull(context, "El contexto es obligatorio");
        DecisionNode node = root;
        while (!node.isLeaf()) {
            node = node.next(node.answer(context));
        }
        return node.getProfile();
    }

    /**
     * Devuelve, en el idioma activo, el camino recorrido. Por ejemplo:
     * "¿Tiene historial? sí → ¿Tiene una marca preferida? no → ... → comprador frecuente".
     */
    public String explain(DecisionContext context) {
        Objects.requireNonNull(context, "El contexto es obligatorio");
        StringBuilder path = new StringBuilder();
        DecisionNode node = root;
        while (!node.isLeaf()) {
            boolean answer = node.answer(context);
            path.append(I18n.text(node.getQuestion()))
                    .append(' ')
                    .append(I18n.text(answer ? "tree.yes" : "tree.no"))
                    .append(" → ");
            node = node.next(answer);
        }
        return path.append(I18n.text("profile." + node.getProfile())).toString();
    }
}
