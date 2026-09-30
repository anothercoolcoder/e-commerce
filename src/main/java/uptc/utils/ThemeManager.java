package uptc.utils;

import java.util.prefs.Preferences;
import javafx.scene.Scene;

/** Aplica el tema claro u oscuro a la escena y recuerda el último tema elegido. */
public final class ThemeManager {
    private static final ThemeManager INSTANCE = new ThemeManager();
    private static final String CSS_FOLDER = "/uptc/css/";

    private final Preferences preferences = Preferences.userRoot().node("shoptree/theme");
    private Theme theme;
    private Scene activeScene;

    private ThemeManager() {
        boolean dark = Theme.DARK.name().equals(preferences.get("theme", Theme.LIGHT.name()));
        theme = dark ? Theme.DARK : Theme.LIGHT;
    }

    /** Devuelve la instancia única. */
    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    public Theme getTheme() {
        return theme;
    }

    /** Alterna entre claro y oscuro y actualiza la escena activa. */
    public void toggle() {
        theme = theme == Theme.LIGHT ? Theme.DARK : Theme.LIGHT;
        preferences.put("theme", theme.name());
        apply(activeScene);
    }

    /**
     * Carga las hojas de estilo en orden: base, colores del tema y componentes.
     * Los colores se definen como variables en light.css / dark.css.
     */
    public void apply(Scene scene) {
        if (scene == null) {
            return;
        }
        activeScene = scene;
        String themeFile = theme == Theme.DARK ? "dark.css" : "light.css";
        scene.getStylesheets().setAll(css("base.css"), css(themeFile), css("components.css"));
    }

    private String css(String file) {
        return ThemeManager.class.getResource(CSS_FOLDER + file).toExternalForm();
    }
}
