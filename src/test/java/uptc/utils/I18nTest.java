package uptc.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class I18nTest {

    @AfterEach
    void restaurarEspanol() {
        I18n.setLanguage("es");
    }

    @Test
    void traduceLaMismaClaveEnLosTresIdiomas() {
        I18n.setLanguage("es");
        assertEquals("Entrar", I18n.text("login.enter"));
        assertEquals("es", I18n.language());

        I18n.setLanguage("en");
        assertEquals("Sign in", I18n.text("login.enter"));
        assertEquals("en", I18n.language());

        I18n.setLanguage("pt");
        assertEquals("Entrar", I18n.text("login.enter"));
        assertEquals("pt", I18n.language());
    }

    @Test
    void aceptaElCodigoEnMayusculasComoLoEnviaElSelectorDeIdioma() {
        I18n.setLanguage("EN");

        assertEquals("en", I18n.language());
    }

    @Test
    void unIdiomaDesconocidoONuloDejaEspanol() {
        I18n.setLanguage("fr");
        assertEquals("es", I18n.language());

        I18n.setLanguage(null);
        assertEquals("es", I18n.language());
    }

    @Test
    void unaClaveInexistenteDevuelveLaPropiaClave() {
        assertEquals("clave.que.no.existe", I18n.text("clave.que.no.existe"));
    }

    @Test
    void losTresArchivosDeIdiomaTienenLasMismasClaves() {
        ResourceBundle es = ResourceBundle.getBundle("uptc.i18n.messages", Locale.forLanguageTag("es"));
        ResourceBundle en = ResourceBundle.getBundle("uptc.i18n.messages", Locale.forLanguageTag("en"));
        ResourceBundle pt = ResourceBundle.getBundle("uptc.i18n.messages", Locale.forLanguageTag("pt"));

        assertEquals(es.keySet(), en.keySet());
        assertEquals(es.keySet(), pt.keySet());
    }
}
