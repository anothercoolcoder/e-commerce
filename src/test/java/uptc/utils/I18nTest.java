package uptc.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    void traduceLaMismaClaveEnEspanolYEnIngles() {
        I18n.setLanguage("es");
        assertEquals("Agregar al carrito", I18n.text("card.add"));
        assertEquals("es", I18n.language());

        I18n.setLanguage("en");
        assertEquals("Add to cart", I18n.text("card.add"));
        assertEquals("en", I18n.language());
    }

    @Test
    void formatReemplazaLosMarcadoresPorLosValores() {
        I18n.setLanguage("es");
        assertEquals("Solo quedan 3", I18n.format("card.lowStock", 3));

        I18n.setLanguage("en");
        assertEquals("Only 3 left", I18n.format("card.lowStock", 3));
    }

    @Test
    void aceptaElCodigoEnMayusculasComoLoEnviaElSelectorDeIdioma() {
        I18n.setLanguage("EN");

        assertEquals("en", I18n.language());
        assertEquals("en", I18n.bundle().getLocale().getLanguage());
    }

    @Test
    void unIdiomaDesconocidoONuloDejaEspanol() {
        I18n.setLanguage("pt");
        assertEquals("es", I18n.language());

        I18n.setLanguage(null);
        assertEquals("es", I18n.language());
    }

    @Test
    void unaClaveInexistenteDevuelveLaPropiaClave() {
        assertEquals("clave.que.no.existe", I18n.text("clave.que.no.existe"));
    }

    @Test
    void losDosArchivosDeIdiomaTienenLasMismasClavesYNingunTextoVacio() {
        ResourceBundle es = ResourceBundle.getBundle("uptc.i18n.messages", Locale.forLanguageTag("es"));
        ResourceBundle en = ResourceBundle.getBundle("uptc.i18n.messages", Locale.forLanguageTag("en"));

        assertEquals(es.keySet(), en.keySet());
        for (String clave : es.keySet()) {
            assertFalse(es.getString(clave).isBlank(), "texto vacío en español: " + clave);
            assertFalse(en.getString(clave).isBlank(), "texto vacío en inglés: " + clave);
        }
    }

    @Test
    void losMensajesDeErrorDeLasReglasDeNegocioTambienSeTraducen() {
        I18n.setLanguage("en");

        assertEquals("Not enough stock for Mouse", I18n.format("error.stock", "Mouse"));
    }
}
