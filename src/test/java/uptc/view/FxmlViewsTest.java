package uptc.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uptc.App;
import uptc.persistence.PersistenceManager;
import uptc.viewController.ShopContext;

/**
 * Carga de verdad cada pantalla FXML con su controlador. Detecta los errores
 * típicos de JavaFX: un fx:id que no existe en el controlador, un método
 * onAction mal escrito o un initialize() que falla con los datos semilla.
 *
 * <p>La tienda se inicia sobre una copia de los datos semilla en una carpeta
 * temporal. La prueba se omite en equipos sin pantalla (servidores de CI).</p>
 */
class FxmlViewsTest {
    @TempDir
    static Path carpeta;

    @BeforeAll
    static void iniciarJavaFxYLaTienda() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "JavaFX necesita una pantalla");
        System.setProperty(PersistenceManager.DATA_PROPERTY, carpeta.toString());
        PersistenceManager.reset();

        CompletableFuture<Void> javaFxListo = new CompletableFuture<>();
        Platform.startup(() -> javaFxListo.complete(null));
        javaFxListo.get(30, TimeUnit.SECONDS);

        ShopContext.initialize();
        ShopContext.setSelectedProduct(ShopContext.products().findAll().get(0));
    }

    @AfterAll
    static void restaurarConfiguracion() {
        System.clearProperty(PersistenceManager.DATA_PROPERTY);
        PersistenceManager.reset();
    }

    @ParameterizedTest
    @ValueSource(strings = {"login", "register", "store", "detail", "cart", "checkout", "account", "admin", "statistics"})
    void cadaPantallaCargaConSuControlador(String pantalla) throws Exception {
        URL fxml = App.class.getResource("fxml/" + pantalla + ".fxml");
        assertNotNull(fxml, "no existe fxml/" + pantalla + ".fxml");

        // Los nodos de JavaFX solo se pueden crear en el hilo de la interfaz.
        CompletableFuture<Parent> carga = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                carga.complete(FXMLLoader.load(fxml));
            } catch (Exception | LinkageError e) {
                carga.completeExceptionally(e);
            }
        });
        Parent raiz = carga.get(60, TimeUnit.SECONDS);

        assertNotNull(raiz);
        assertTrue(raiz.getStyleClass().contains("shop-root") || raiz.getStyleClass().contains("auth-root"));
    }
}
