package uptc.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import uptc.controller.tree.DecisionContext;
import uptc.controller.tree.DecisionTree;
import uptc.controller.tree.RecommendationProfile;
import uptc.model.Compra;
import uptc.model.DetalleCompra;
import uptc.model.Interaccion;
import uptc.model.Producto;
import uptc.model.TipoInteraccion;

/**
 * Recomendaciones personalizadas a partir del historial de cada usuario.
 *
 * <ol>
 *   <li>Se reúne el historial: sus compras (persistidas en JSON) y sus
 *       interacciones de la sesión (clics, carrito...), cada una con un peso.</li>
 *   <li>Del historial se obtiene un {@link DecisionContext}: categoría favorita,
 *       marca preferida, precio promedio y número de compras.</li>
 *   <li>El árbol de decisión clasifica ese contexto en un perfil.</li>
 *   <li>El perfil decide qué productos del catálogo se recomiendan.</li>
 * </ol>
 */
public class RecomendacionController {
    private final ProductoController products;
    private final CompraController purchases;
    private final DecisionTree decisionTree;
    private final List<Interaccion> interactions;

    public RecomendacionController(ProductoController products, CompraController purchases,
            DecisionTree decisionTree, List<Interaccion> interactions) {
        this.products = Objects.requireNonNull(products);
        this.purchases = Objects.requireNonNull(purchases);
        this.decisionTree = Objects.requireNonNull(decisionTree);
        this.interactions = Objects.requireNonNull(interactions);
    }

    /** Devuelve hasta {@code limit} productos recomendados, los mejor calificados primero. */
    public List<Producto> forUser(String userId, int limit) {
        DecisionContext context = contextFor(userId);
        RecommendationProfile profile = decisionTree.classify(context);
        Set<String> purchased = purchasedIds(userId);

        List<Producto> recommended = candidates(purchased, profile, context);
        if (recommended.isEmpty()) {
            // Si el perfil no encuentra productos, se recomiendan los mejor calificados del catálogo.
            recommended = candidates(purchased, RecommendationProfile.EXPLORADOR_NUEVO, context);
        }
        recommended.sort(Comparator.comparingDouble(Producto::getCalificacion).reversed());
        return List.copyOf(recommended.subList(0, Math.min(Math.max(limit, 0), recommended.size())));
    }

    /** Perfil en el que el árbol de decisión clasifica al usuario. */
    public RecommendationProfile profileFor(String userId) {
        return decisionTree.classify(contextFor(userId));
    }

    /** Devuelve el camino que siguió el árbol de decisión para clasificar al usuario. */
    public String explain(String userId) {
        return decisionTree.explain(contextFor(userId));
    }

    /** Resume el historial del usuario en los datos que evalúa el árbol de decisión. */
    public DecisionContext contextFor(String userId) {
        Map<String, Double> weightByCategory = new TreeMap<>();
        Map<String, Double> weightByBrand = new TreeMap<>();
        Map<String, Integer> productsByBrand = new HashMap<>();
        double totalWeight = 0;
        double priceSum = 0;
        int productCount = 0;

        for (Map.Entry<String, Double> entry : weightByProduct(userId).entrySet()) {
            if (!products.exists(entry.getKey())) {
                continue;
            }
            Producto product = products.find(entry.getKey());
            double weight = entry.getValue();
            weightByCategory.merge(text(product.getCategoriaId()), weight, Double::sum);
            weightByBrand.merge(text(product.getMarca()), weight, Double::sum);
            productsByBrand.merge(text(product.getMarca()), 1, Integer::sum);
            totalWeight += weight;
            priceSum += product.getPrecio();
            productCount++;
        }

        int purchaseCount = purchases.history(userId).size();
        if (productCount == 0) {
            return new DecisionContext("", 0, "", purchaseCount);
        }
        String favoriteBrand = heaviest(weightByBrand);
        // Una marca es "preferida" solo si el usuario la eligió en al menos dos productos
        // distintos y concentra al menos la mitad del peso de su historial.
        boolean brandDominates = productsByBrand.get(favoriteBrand) >= 2
                && weightByBrand.get(favoriteBrand) >= totalWeight / 2;
        return new DecisionContext(heaviest(weightByCategory), priceSum / productCount,
                brandDominates ? favoriteBrand : "", purchaseCount);
    }

    /** Peso acumulado de cada producto del historial: compras del usuario más interacciones de la sesión. */
    private Map<String, Double> weightByProduct(String userId) {
        Map<String, Double> weights = new HashMap<>();
        for (Compra purchase : purchases.history(userId)) {
            for (DetalleCompra detail : purchase.getDetalles()) {
                weights.merge(detail.getProductoId(), TipoInteraccion.COMPRA.getPeso(), Double::sum);
            }
        }
        for (Interaccion interaction : interactions) {
            if (Objects.equals(userId, interaction.getUsuarioId())) {
                weights.merge(interaction.getProductoId(), interaction.getPeso(), Double::sum);
            }
        }
        return weights;
    }

    /** Ids de los productos que el usuario ya compró: no tiene sentido volver a recomendarlos. */
    private Set<String> purchasedIds(String userId) {
        Set<String> ids = new HashSet<>();
        for (Compra purchase : purchases.history(userId)) {
            for (DetalleCompra detail : purchase.getDetalles()) {
                ids.add(detail.getProductoId());
            }
        }
        return ids;
    }

    /** Productos disponibles, que el usuario no ha comprado y que corresponden al perfil. */
    private List<Producto> candidates(Set<String> purchased, RecommendationProfile profile,
            DecisionContext context) {
        List<Producto> result = new ArrayList<>();
        for (Producto product : products.findAll()) {
            boolean available = product.isActivo() && product.getStock() > 0;
            if (available && !purchased.contains(product.getId()) && matches(product, profile, context)) {
                result.add(product);
            }
        }
        return result;
    }

    private boolean matches(Producto product, RecommendationProfile profile, DecisionContext context) {
        boolean favoriteCategory = context.categoria().equals(product.getCategoriaId());
        return switch (profile) {
            case EXPLORADOR_NUEVO -> true;
            case MARCA_PREFERIDA -> context.marca().equalsIgnoreCase(product.getMarca());
            case COMPRADOR_FRECUENTE -> favoriteCategory && product.getDescuento() > 0;
            case CATEGORIA_PREMIUM -> favoriteCategory && product.getPrecio() > DecisionTree.PRECIO_PREMIUM;
            case CATEGORIA_ECONOMICA -> favoriteCategory && product.getPrecio() <= DecisionTree.PRECIO_PREMIUM;
        };
    }

    /** Devuelve la clave con mayor peso; en caso de empate gana la primera en orden alfabético. */
    private String heaviest(Map<String, Double> weights) {
        String best = "";
        double bestWeight = -1;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            if (entry.getValue() > bestWeight) {
                best = entry.getKey();
                bestWeight = entry.getValue();
            }
        }
        return best;
    }

    private String text(String value) {
        return value == null ? "" : value;
    }
}
