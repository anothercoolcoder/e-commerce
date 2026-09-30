package uptc.controller.tree;

/** Clave compuesta que ordena primero por precio y luego por ID. */
public record PriceKey(double price, String id) implements Comparable<PriceKey> {
    @Override public int compareTo(PriceKey other) {
        int byPrice = Double.compare(price, other.price);
        return byPrice != 0 ? byPrice : id.compareTo(other.id);
    }
}
