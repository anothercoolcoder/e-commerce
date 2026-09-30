package uptc.model;

/**
 * Tipos de interacción de un usuario con un producto. El peso indica cuánto
 * interés demuestra cada una: una compra dice más que una simple vista.
 */
public enum TipoInteraccion {
    VISTA(1),
    BUSQUEDA(2),
    CLIC(3),
    FAVORITO(5),
    CARRITO(7),
    COMPRA(10);

    private final double peso;

    TipoInteraccion(double peso) {
        this.peso = peso;
    }

    public double getPeso() {
        return peso;
    }
}
