package uptc.exception;

/** Se lanza ante credenciales inválidas o usuarios no autorizados. */
public class AutenticacionException extends RuntimeException {
    public AutenticacionException(String message) { super(message); }
}
