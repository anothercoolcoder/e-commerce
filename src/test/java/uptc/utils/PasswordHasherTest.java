package uptc.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    @Test
    void elHashEsSha256EnHexadecimalYNoContieneLaContrasena() {
        String hash = PasswordHasher.hash("secreto");

        assertEquals(64, hash.length());
        assertNotEquals("secreto", hash);
    }

    @Test
    void laMismaContrasenaSiempreProduceElMismoHash() {
        assertEquals(PasswordHasher.hash("secreto"), PasswordHasher.hash("secreto"));
        assertNotEquals(PasswordHasher.hash("secreto"), PasswordHasher.hash("Secreto"));
    }

    @Test
    void noSePuedeCalcularElHashDeUnaContrasenaVacia() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(""));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(null));
    }

    @Test
    void comparaLaContrasenaContraElHashGuardado() {
        String guardado = PasswordHasher.hash("secreto");

        assertTrue(PasswordHasher.matches("secreto", guardado));
        assertFalse(PasswordHasher.matches("otra", guardado));
    }

    @Test
    void aceptaLasContrasenasEnTextoPlanoDelArchivoSemilla() {
        assertTrue(PasswordHasher.matches("admin123", "admin123"));
        assertFalse(PasswordHasher.matches("admin124", "admin123"));
    }

    @Test
    void unaContrasenaVaciaOUnValorGuardadoNuloNuncaCoinciden() {
        assertFalse(PasswordHasher.matches("", PasswordHasher.hash("secreto")));
        assertFalse(PasswordHasher.matches(null, "algo"));
        assertFalse(PasswordHasher.matches("secreto", null));
    }
}
