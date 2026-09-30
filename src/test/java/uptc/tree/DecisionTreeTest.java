package uptc.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uptc.controller.tree.DecisionContext;
import uptc.controller.tree.DecisionTree;
import uptc.controller.tree.RecommendationProfile;
import uptc.utils.I18n;

class DecisionTreeTest {
    private final DecisionTree arbol = new DecisionTree();

    @Test
    void usuarioSinHistorialEsExploradorNuevo() {
        DecisionContext sinHistorial = new DecisionContext("", 0, "", 0);

        assertEquals(RecommendationProfile.EXPLORADOR_NUEVO, arbol.classify(sinHistorial));
    }

    @Test
    void laMarcaPreferidaSeEvaluaAntesQueLasComprasYElPrecio() {
        DecisionContext conMarca = new DecisionContext("Tecnología", 900_000, "Nova", 8);

        assertEquals(RecommendationProfile.MARCA_PREFERIDA, arbol.classify(conMarca));
    }

    @Test
    void cincoComprasOMasEsCompradorFrecuente() {
        assertEquals(RecommendationProfile.COMPRADOR_FRECUENTE,
                arbol.classify(new DecisionContext("Hogar", 100_000, "", 5)));
        assertEquals(RecommendationProfile.CATEGORIA_ECONOMICA,
                arbol.classify(new DecisionContext("Hogar", 100_000, "", 4)));
    }

    @Test
    void elPrecioPromedioSeparaPremiumDeEconomica() {
        assertEquals(RecommendationProfile.CATEGORIA_PREMIUM,
                arbol.classify(new DecisionContext("Tecnología", 700_000, "", 0)));
        assertEquals(RecommendationProfile.CATEGORIA_ECONOMICA,
                arbol.classify(new DecisionContext("Tecnología", 500_000, "", 0)));
    }

    @Test
    void unContextoConDatosNulosSeTrataComoSinHistorial() {
        DecisionContext conNulos = new DecisionContext(null, 0, null, 0);

        assertEquals("", conNulos.categoria());
        assertEquals("", conNulos.marca());
        assertEquals(RecommendationProfile.EXPLORADOR_NUEVO, arbol.classify(conNulos));
    }

    @Test
    void explainDescribeElCaminoRecorridoEnElIdiomaActivo() {
        DecisionContext compradorFrecuente = new DecisionContext("Hogar", 100_000, "", 5);

        I18n.setLanguage("es");
        assertEquals("¿Tiene historial? sí → ¿Tiene una marca preferida? no → "
                + "¿Ha hecho 5 compras o más? sí → comprador frecuente", arbol.explain(compradorFrecuente));

        I18n.setLanguage("en");
        assertEquals("Has history? yes → Has a preferred brand? no → "
                + "Made 5 purchases or more? yes → frequent buyer", arbol.explain(compradorFrecuente));
    }

    @AfterEach
    void restaurarEspanol() {
        I18n.setLanguage("es");
    }

    @Test
    void elContextoEsObligatorio() {
        assertThrows(NullPointerException.class, () -> arbol.classify(null));
        assertThrows(NullPointerException.class, () -> arbol.explain(null));
    }
}
