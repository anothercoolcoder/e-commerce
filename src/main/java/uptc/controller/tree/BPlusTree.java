package uptc.controller.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Árbol B+ genérico implementado manualmente.
 *
 * <p>Todos los valores se almacenan en las hojas. Los nodos internos solo
 * guardan claves separadoras para saber por cuál hijo bajar, y las hojas están
 * enlazadas entre sí para recorrer un rango sin volver a subir por el árbol.</p>
 *
 * <p>Con orden {@code m}, un nodo interno tiene como máximo {@code m} hijos y
 * una hoja como máximo {@code m - 1} claves; al superar ese máximo el nodo se
 * divide en dos. Al eliminar, un nodo que queda por debajo de su mínimo pide
 * prestado a un hermano o se fusiona con él.</p>
 *
 * @param <K> tipo de la clave
 * @param <V> tipo del valor
 */
public class BPlusTree<K extends Comparable<? super K>, V> {
    private final int order;
    private Node<K, V> root;
    private int size;

    /** Crea un árbol con el orden indicado (mínimo 3). */
    public BPlusTree(int order) {
        if (order < 3) {
            throw new IllegalArgumentException("El orden B+ debe ser al menos 3");
        }
        this.order = order;
        this.root = new Leaf<>();
    }

    /** Devuelve el orden configurado. */
    public int getOrder() {
        return order;
    }

    /** Devuelve la cantidad de claves almacenadas. */
    public int size() {
        return size;
    }

    /** Indica si el árbol no contiene claves. */
    public boolean isEmpty() {
        return size == 0;
    }

    /** Inserta o reemplaza el valor asociado a la clave. */
    public void insert(K key, V value) {
        Objects.requireNonNull(key, "La clave es obligatoria");
        Objects.requireNonNull(value, "El valor es obligatorio");
        Leaf<K, V> leaf = findLeaf(key);
        int position = binarySearch(leaf.entries, key);
        if (position >= 0) {
            leaf.entries.set(position, new Entry<>(key, value));
            return;
        }
        leaf.entries.add(-position - 1, new Entry<>(key, value));
        size++;
        if (leaf.entries.size() >= order) {
            splitLeaf(leaf);
        } else {
            refreshAncestors(leaf.parent);
        }
    }

    /** Busca una clave exacta. Devuelve null si no existe. */
    public V search(K key) {
        if (key == null || size == 0) {
            return null;
        }
        Leaf<K, V> leaf = findLeaf(key);
        int position = binarySearch(leaf.entries, key);
        return position >= 0 ? leaf.entries.get(position).value : null;
    }

    /** Elimina una clave y devuelve el valor eliminado, o null si no existe. */
    public V delete(K key) {
        if (key == null || size == 0) {
            return null;
        }
        Leaf<K, V> leaf = findLeaf(key);
        int position = binarySearch(leaf.entries, key);
        if (position < 0) {
            return null;
        }
        V removed = leaf.entries.remove(position).value;
        size--;
        rebalanceLeaf(leaf);
        return removed;
    }

    /**
     * Devuelve los valores cuyas claves están dentro del intervalo inclusivo.
     * Baja una sola vez hasta la hoja del mínimo y después avanza por las hojas
     * enlazadas hasta pasar el máximo.
     */
    public List<V> range(K minimum, K maximum) {
        if (minimum == null || maximum == null || minimum.compareTo(maximum) > 0) {
            return List.of();
        }
        List<V> result = new ArrayList<>();
        Leaf<K, V> leaf = findLeaf(minimum);
        while (leaf != null) {
            for (Entry<K, V> entry : leaf.entries) {
                if (entry.key.compareTo(minimum) < 0) {
                    continue;
                }
                if (entry.key.compareTo(maximum) > 0) {
                    return result;
                }
                result.add(entry.value);
            }
            leaf = leaf.next;
        }
        return result;
    }

    /** Devuelve todos los valores ordenados por clave recorriendo las hojas enlazadas. */
    public List<V> values() {
        List<V> result = new ArrayList<>();
        Leaf<K, V> leaf = firstLeaf();
        while (leaf != null) {
            for (Entry<K, V> entry : leaf.entries) {
                result.add(entry.value);
            }
            leaf = leaf.next;
        }
        return result;
    }

    /** Devuelve las claves de la raíz; permite observar cómo se divide el árbol. */
    public List<K> rootSeparators() {
        if (root instanceof Leaf<K, V> leaf) {
            List<K> keys = new ArrayList<>();
            for (Entry<K, V> entry : leaf.entries) {
                keys.add(entry.key);
            }
            return keys;
        }
        return List.copyOf(((Internal<K, V>) root).keys);
    }

    // ------------------------------------------------------------ búsqueda

    /** Baja desde la raíz hasta la hoja donde está (o debería estar) la clave. */
    private Leaf<K, V> findLeaf(K key) {
        Node<K, V> current = root;
        while (current instanceof Internal<K, V> internal) {
            int child = 0;
            while (child < internal.keys.size() && key.compareTo(internal.keys.get(child)) >= 0) {
                child++;
            }
            current = internal.children.get(child);
        }
        return (Leaf<K, V>) current;
    }

    private Leaf<K, V> firstLeaf() {
        Node<K, V> current = root;
        while (current instanceof Internal<K, V> internal) {
            current = internal.children.get(0);
        }
        return (Leaf<K, V>) current;
    }

    /** Búsqueda binaria en una hoja. Si no está, devuelve -(posición de inserción) - 1. */
    private int binarySearch(List<Entry<K, V>> entries, K key) {
        int low = 0;
        int high = entries.size() - 1;
        while (low <= high) {
            int middle = (low + high) / 2;
            int comparison = entries.get(middle).key.compareTo(key);
            if (comparison < 0) {
                low = middle + 1;
            } else if (comparison > 0) {
                high = middle - 1;
            } else {
                return middle;
            }
        }
        return -low - 1;
    }

    // ------------------------------------------------------------ división (inserción)

    /** Divide una hoja llena en dos mitades y enlaza la nueva hoja a la derecha. */
    private void splitLeaf(Leaf<K, V> leaf) {
        int middle = leaf.entries.size() / 2;
        Leaf<K, V> right = new Leaf<>();
        right.entries.addAll(leaf.entries.subList(middle, leaf.entries.size()));
        leaf.entries.subList(middle, leaf.entries.size()).clear();
        right.next = leaf.next;
        leaf.next = right;
        attachRightSibling(leaf, right);
    }

    /** Registra el nuevo hermano derecho en el padre; si no hay padre, crea una raíz nueva. */
    private void attachRightSibling(Node<K, V> left, Node<K, V> right) {
        if (left.parent == null) {
            Internal<K, V> newRoot = new Internal<>();
            newRoot.children.add(left);
            newRoot.children.add(right);
            left.parent = newRoot;
            right.parent = newRoot;
            root = newRoot;
            refresh(newRoot);
            return;
        }
        Internal<K, V> parent = left.parent;
        int index = parent.children.indexOf(left);
        parent.children.add(index + 1, right);
        right.parent = parent;
        refresh(parent);
        if (parent.children.size() > order) {
            splitInternal(parent);
        } else {
            refreshAncestors(parent.parent);
        }
    }

    /** Divide un nodo interno lleno; la división puede propagarse hasta la raíz. */
    private void splitInternal(Internal<K, V> node) {
        int middle = node.children.size() / 2;
        Internal<K, V> right = new Internal<>();
        right.children.addAll(node.children.subList(middle, node.children.size()));
        node.children.subList(middle, node.children.size()).clear();
        for (Node<K, V> child : right.children) {
            child.parent = right;
        }
        refresh(node);
        refresh(right);
        attachRightSibling(node, right);
    }

    // ------------------------------------------------------------ rebalanceo (eliminación)

    /**
     * Si la hoja quedó con menos claves que el mínimo, primero intenta pedir una
     * prestada a un hermano; si ninguno puede prestar, se fusiona con un hermano.
     */
    private void rebalanceLeaf(Leaf<K, V> leaf) {
        if (leaf.parent == null) {
            return;
        }
        int minimum = Math.max(1, (order - 1) / 2);
        if (leaf.entries.size() >= minimum) {
            refreshAncestors(leaf.parent);
            return;
        }
        Internal<K, V> parent = leaf.parent;
        int index = parent.children.indexOf(leaf);
        Leaf<K, V> left = index > 0 ? (Leaf<K, V>) parent.children.get(index - 1) : null;
        Leaf<K, V> right = index + 1 < parent.children.size() ? (Leaf<K, V>) parent.children.get(index + 1) : null;

        if (left != null && left.entries.size() > minimum) {
            leaf.entries.add(0, left.entries.remove(left.entries.size() - 1));
        } else if (right != null && right.entries.size() > minimum) {
            leaf.entries.add(right.entries.remove(0));
        } else if (left != null) {
            left.entries.addAll(leaf.entries);
            left.next = leaf.next;
            parent.children.remove(index);
        } else if (right != null) {
            leaf.entries.addAll(right.entries);
            leaf.next = right.next;
            parent.children.remove(index + 1);
        }
        rebalanceInternal(parent);
    }

    /** Igual que en las hojas, pero prestando o fusionando hijos entre nodos internos. */
    private void rebalanceInternal(Internal<K, V> node) {
        refresh(node);
        if (node == root) {
            // Una raíz interna con un solo hijo sobra: el árbol pierde un nivel.
            if (node.children.size() == 1) {
                root = node.children.get(0);
                root.parent = null;
            }
            return;
        }
        int minimum = (order + 1) / 2;
        if (node.children.size() >= minimum) {
            refreshAncestors(node.parent);
            return;
        }
        Internal<K, V> parent = node.parent;
        int index = parent.children.indexOf(node);
        Internal<K, V> left = index > 0 ? (Internal<K, V>) parent.children.get(index - 1) : null;
        Internal<K, V> right = index + 1 < parent.children.size() ? (Internal<K, V>) parent.children.get(index + 1) : null;

        if (left != null && left.children.size() > minimum) {
            Node<K, V> child = left.children.remove(left.children.size() - 1);
            child.parent = node;
            node.children.add(0, child);
        } else if (right != null && right.children.size() > minimum) {
            Node<K, V> child = right.children.remove(0);
            child.parent = node;
            node.children.add(child);
        } else if (left != null) {
            left.children.addAll(node.children);
            for (Node<K, V> child : left.children) {
                child.parent = left;
            }
            parent.children.remove(index);
        } else if (right != null) {
            node.children.addAll(right.children);
            for (Node<K, V> child : node.children) {
                child.parent = node;
            }
            parent.children.remove(index + 1);
        }
        // Los nodos que ganaron o perdieron hijos deben recalcular sus separadores.
        refresh(node);
        if (left != null) {
            refresh(left);
        }
        if (right != null) {
            refresh(right);
        }
        rebalanceInternal(parent);
    }

    // ------------------------------------------------------------ separadores

    private void refreshAncestors(Internal<K, V> node) {
        while (node != null) {
            refresh(node);
            node = node.parent;
        }
    }

    /** Recalcula los separadores: cada uno es la primera clave del hijo que tiene a su derecha. */
    private void refresh(Internal<K, V> node) {
        node.keys.clear();
        for (int i = 1; i < node.children.size(); i++) {
            node.keys.add(firstKey(node.children.get(i)));
        }
    }

    private K firstKey(Node<K, V> node) {
        Node<K, V> current = node;
        while (current instanceof Internal<K, V> internal) {
            current = internal.children.get(0);
        }
        return ((Leaf<K, V>) current).entries.get(0).key;
    }

    // ------------------------------------------------------------ nodos

    private static final class Entry<K, V> {
        private final K key;
        private final V value;

        private Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private abstract static class Node<K, V> {
        Internal<K, V> parent;
    }

    /** Hoja: guarda las parejas clave-valor y un enlace a la hoja siguiente. */
    private static final class Leaf<K, V> extends Node<K, V> {
        private final List<Entry<K, V>> entries = new ArrayList<>();
        private Leaf<K, V> next;
    }

    /** Nodo interno: solo guarda separadores y referencias a sus hijos. */
    private static final class Internal<K, V> extends Node<K, V> {
        private final List<K> keys = new ArrayList<>();
        private final List<Node<K, V>> children = new ArrayList<>();
    }
}
