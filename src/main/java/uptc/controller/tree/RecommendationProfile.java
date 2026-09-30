package uptc.controller.tree;

/** Perfil de recomendación: cada hoja del árbol de decisión produce uno. */
public enum RecommendationProfile {
    /** Usuario sin historial: se le muestran los productos mejor calificados. */
    EXPLORADOR_NUEVO,
    /** Una marca concentra su historial: se le muestran productos de esa marca. */
    MARCA_PREFERIDA,
    /** Compra con frecuencia: se le muestran ofertas de su categoría favorita. */
    COMPRADOR_FRECUENTE,
    /** Suele ver productos costosos: gama alta de su categoría favorita. */
    CATEGORIA_PREMIUM,
    /** Suele ver productos económicos: gama baja de su categoría favorita. */
    CATEGORIA_ECONOMICA
}
