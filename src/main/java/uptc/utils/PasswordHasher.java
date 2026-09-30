package uptc.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Utilidad de hash SHA-256 para el flujo académico de autenticación. */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    /** Calcula el hash hexadecimal de una contraseña. */
    public static String hash(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    /**
     * Comprueba una contraseña contra el valor almacenado. Los usuarios que se
     * registran en la aplicación guardan el hash; los usuarios del archivo
     * semilla traen la contraseña de demostración en texto plano, por eso
     * también se acepta la comparación directa.
     */
    public static boolean matches(String password, String stored) {
        if (password == null || password.isBlank() || stored == null) {
            return false;
        }
        return stored.equals(hash(password)) || stored.equals(password);
    }
}
