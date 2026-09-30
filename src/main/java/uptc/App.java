package uptc;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uptc.utils.I18n;
import uptc.utils.ThemeManager;
import uptc.viewController.ShopContext;

/**
 * Punto de entrada de la aplicación JavaFX. Prepara los datos de la tienda,
 * crea la ventana y muestra la pantalla de inicio de sesión.
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        ShopContext.initialize();
        scene = new Scene(loadFXML("fxml/login"), 1180, 760);
        ThemeManager.getInstance().apply(scene);
        stage.setTitle("ShopTree · Recomendador de productos");
        stage.setMinWidth(980);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    /** Reemplaza el contenido de la ventana por el FXML indicado (ruta relativa a resources/uptc). */
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /** Carga el FXML con los textos del idioma activo: en el FXML, text="%clave" se traduce aquí. */
    private static Parent loadFXML(String fxml) throws IOException {
        return new FXMLLoader(App.class.getResource(fxml + ".fxml"), I18n.bundle()).load();
    }

    public static void main(String[] args) {
        launch();
    }
}
