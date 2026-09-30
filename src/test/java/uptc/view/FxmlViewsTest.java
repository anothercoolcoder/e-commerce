package uptc.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
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
import uptc.utils.I18n;
import uptc.viewController.ShopContext;

/**
 * Carga de verdad cada pantalla FXML con su controlador, en español y en
 * inglés. Detecta los errores típicos de JavaFX: un fx:id que no existe en el
 * controlador, un método onAction mal escrito, una clave de idioma (%clave)
 * que falta o un initialize() que falla con los datos semilla.
 *
 * <p>La tienda se inicia sobre una copia de los datos semilla en una carpeta
 * temporal y con el usuario administrador. La prueba se omite en equipos sin
 * pantalla (servidores de CI).</p>
 */
class FxmlViewsTest {
    private static final List<String> PANTALLAS = List.of(
            "login", "register", "store", "detail", "cart", "checkout", "account", "admin", "statistics");

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
        I18n.setLanguage("es");
    }

    @ParameterizedTest
    @ValueSource(strings = {"es", "en"})
    void todasLasPantallasCarganEnElIdioma(String idioma) throws Exception {
        I18n.setLanguage(idioma);

        for (String pantalla : PANTALLAS) {
            assertNotNull(cargar(pantalla), pantalla + " (" + idioma + ")");
        }
    }

    /** Carga el FXML igual que la aplicación: con los textos del idioma activo y en el hilo de JavaFX. */
    private Parent cargar(String pantalla) throws Exception {
        URL fxml = App.class.getResource("fxml/" + pantalla + ".fxml");
        assertNotNull(fxml, "no existe fxml/" + pantalla + ".fxml");

        CompletableFuture<Parent> carga = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                carga.complete(FXMLLoader.load(fxml, I18n.bundle()));
            } catch (Exception | LinkageError e) {
                carga.completeExceptionally(new AssertionError("no cargó " + pantalla, e));
            }
        });
        return carga.get(60, TimeUnit.SECONDS);
    }
}
