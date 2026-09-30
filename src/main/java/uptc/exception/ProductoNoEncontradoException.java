package uptc.exception;

/** Se lanza cuando el catálogo no contiene el producto solicitado. */
public class ProductoNoEncontradoException extends RuntimeException {
    public ProductoNoEncontradoException(String message) { super(message); }
}
