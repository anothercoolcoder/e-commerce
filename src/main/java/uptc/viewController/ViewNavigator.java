package uptc.viewController;

import java.io.IOException;
import uptc.App;

/** Navegación entre las pantallas FXML de la carpeta {@code resources/uptc/fxml}. */
public final class ViewNavigator {

    private ViewNavigator() {
    }

    /**
     * Cambia la pantalla actual. Si el FXML no se puede cargar es un error de
     * programación (archivo o controlador mal escrito), por eso se relanza
     * como excepción no comprobada en lugar de obligar a cada vista a capturarla.
     */
    public static void go(String view) {
        try {
            App.setRoot("fxml/" + view);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo abrir la vista " + view, e);
        }
    }
}
