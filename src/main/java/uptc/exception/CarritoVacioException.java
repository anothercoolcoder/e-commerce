package uptc.exception;

/** Se lanza al intentar confirmar un carrito sin productos. */
public class CarritoVacioException extends RuntimeException {
    public CarritoVacioException(String message) { super(message); }
}
