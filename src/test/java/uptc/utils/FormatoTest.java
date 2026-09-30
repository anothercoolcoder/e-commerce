package uptc.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FormatoTest {

    @Test
    void formateaPesosConSeparadorDeMilesYSinDecimales() {
        assertEquals("$1.250.000", Formato.moneda(1_250_000));
        assertEquals("$0", Formato.moneda(0));
    }

    @Test
    void redondeaLosDecimales() {
        assertEquals("$90.000", Formato.moneda(89_999.6));
    }
}
