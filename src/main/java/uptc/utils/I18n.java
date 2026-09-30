package uptc.utils;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Internacionalización: carga los textos de
 * {@code resources/uptc/i18n/messages_<idioma>.properties} (es, en) y
 * recuerda el último idioma elegido.
 */
public final class I18n {
    private static final String BUNDLE = "uptc.i18n.messages";
    private static final Preferences PREFERENCES = Preferences.userRoot().node("shoptree/language");

    private static ResourceBundle bundle;

    static {
        load(PREFERENCES.get("locale", "es"));
    }

    private I18n() {
    }

    /** Cambia el idioma por su código (es o en). Un código desconocido deja español. */
    public static void setLanguage(String code) {
        load(code);
        PREFERENCES.put("locale", language());
    }

    /** Devuelve la traducción de la clave; si la clave no existe devuelve la propia clave. */
    public static String text(String key) {
        return bundle.containsKey(key) ? bundle.getString(key) : key;
    }

    /** Traduce una clave cuyo texto tiene marcadores ({@code %s}, {@code %d}) y los reemplaza por los valores. */
    public static String format(String key, Object... values) {
        return String.format(text(key), values);
    }

    /** Código del idioma activo. */
    public static String language() {
        return bundle.getLocale().getLanguage();
    }

    /** Textos del idioma activo; los archivos FXML los usan para resolver {@code text="%clave"}. */
    public static ResourceBundle bundle() {
        return bundle;
    }

    private static void load(String code) {
        String language = code == null ? "" : code.trim().toLowerCase(Locale.ROOT);
        if (!language.equals("en")) {
            language = "es";
        }
        bundle = ResourceBundle.getBundle(BUNDLE, Locale.forLanguageTag(language));
    }
}
