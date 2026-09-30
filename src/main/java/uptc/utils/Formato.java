package uptc.utils;

import java.util.Locale;

/** Conversores de datos a texto para mostrarlos en pantalla. */
public final class Formato {
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");

    private Formato() {
    }

    /** Formatea un valor en pesos colombianos sin decimales, por ejemplo $1.250.000. */
    public static String moneda(double valor) {
        return String.format(COLOMBIA, "$%,.0f", valor);
    }
}
