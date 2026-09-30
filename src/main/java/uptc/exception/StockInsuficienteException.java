package uptc.exception;

/** Se lanza cuando una operación solicita más unidades que las disponibles. */
public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(String message) { super(message); }
}
