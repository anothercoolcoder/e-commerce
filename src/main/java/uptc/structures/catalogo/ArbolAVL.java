package uptc.structures.catalogo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import uptc.model.Producto;

/**
 * Árbol AVL del catálogo: árbol binario de búsqueda autobalanceado cuya clave
 * es el id del producto.
 *
 * <p>Después de cada inserción o eliminación se revisa el factor de balance
 * (altura izquierda − altura derecha) de los nodos del camino recorrido. Si
 * queda fuera de [-1, 1] se corrige con rotaciones, por lo que la búsqueda por
 * id siempre es O(log n).</p>
 */
public class ArbolAVL implements ArbolCatalogo {

    private NodoArbolCatalogo raiz;
    private int tamano;

    /** Inserta el producto. Si el id ya existe, reemplaza el producto guardado. */
    @Override
    public void insertar(Producto producto) {
        if (producto == null || producto.getId() == null) {
            throw new IllegalArgumentException("Producto e id obligatorios");
        }
        boolean esNuevo = buscar(producto.getId()).isEmpty();
        raiz = insertar(raiz, producto);
        if (esNuevo) {
            tamano++;
        }
    }

    private NodoArbolCatalogo insertar(NodoArbolCatalogo nodo, Producto producto) {
        if (nodo == null) {
            return new NodoArbolCatalogo(producto);
        }
        int comparacion = producto.getId().compareTo(nodo.getProducto().getId());
        if (comparacion < 0) {
            nodo.setIzquierdo(insertar(nodo.getIzquierdo(), producto));
        } else if (comparacion > 0) {
            nodo.setDerecho(insertar(nodo.getDerecho(), producto));
        } else {
            nodo.setProducto(producto);
            return nodo;
        }
        return balancear(nodo);
    }

    /** Busca por id bajando por la izquierda o la derecha según la comparación. */
    @Override
    public Optional<Producto> buscar(String id) {
        NodoArbolCatalogo actual = raiz;
        while (actual != null && id != null) {
            int comparacion = id.compareTo(actual.getProducto().getId());
            if (comparacion == 0) {
                return Optional.of(actual.getProducto());
            }
            actual = comparacion < 0 ? actual.getIzquierdo() : actual.getDerecho();
        }
        return Optional.empty();
    }

    /** Elimina el producto con ese id. Devuelve false si no existía. */
    @Override
    public boolean eliminar(String id) {
        if (buscar(id).isEmpty()) {
            return false;
        }
        raiz = eliminar(raiz, id);
        tamano--;
        return true;
    }

    private NodoArbolCatalogo eliminar(NodoArbolCatalogo nodo, String id) {
        if (nodo == null) {
            return null;
        }
        int comparacion = id.compareTo(nodo.getProducto().getId());
        if (comparacion < 0) {
            nodo.setIzquierdo(eliminar(nodo.getIzquierdo(), id));
        } else if (comparacion > 0) {
            nodo.setDerecho(eliminar(nodo.getDerecho(), id));
        } else {
            // Caso 1 y 2: sin hijos o con un solo hijo, el hijo (o null) ocupa su lugar.
            if (nodo.getIzquierdo() == null) {
                return nodo.getDerecho();
            }
            if (nodo.getDerecho() == null) {
                return nodo.getIzquierdo();
            }
            // Caso 3: dos hijos. Se copia el sucesor (el menor del subárbol derecho)
            // y se elimina ese sucesor de su posición original.
            NodoArbolCatalogo sucesor = nodo.getDerecho();
            while (sucesor.getIzquierdo() != null) {
                sucesor = sucesor.getIzquierdo();
            }
            nodo.setProducto(sucesor.getProducto());
            nodo.setDerecho(eliminar(nodo.getDerecho(), sucesor.getProducto().getId()));
        }
        return balancear(nodo);
    }

    /** Devuelve los productos ordenados por id (recorrido izquierda, raíz, derecha). */
    @Override
    public List<Producto> obtenerInOrder() {
        List<Producto> resultado = new ArrayList<>();
        inOrder(raiz, resultado);
        return resultado;
    }

    private void inOrder(NodoArbolCatalogo nodo, List<Producto> resultado) {
        if (nodo != null) {
            inOrder(nodo.getIzquierdo(), resultado);
            resultado.add(nodo.getProducto());
            inOrder(nodo.getDerecho(), resultado);
        }
    }

    /**
     * Filtra por precio recorriendo todo el árbol: el AVL está ordenado por id,
     * no por precio. Para rangos de precio eficientes se usa el árbol B+.
     */
    @Override
    public List<Producto> buscarPorRangoPrecio(double precioMinimo, double precioMaximo) {
        List<Producto> resultado = new ArrayList<>();
        for (Producto producto : obtenerInOrder()) {
            if (producto.getPrecio() >= precioMinimo && producto.getPrecio() <= precioMaximo) {
                resultado.add(producto);
            }
        }
        return resultado;
    }

    @Override
    public void vaciar() {
        raiz = null;
        tamano = 0;
    }

    @Override
    public int getTamano() {
        return tamano;
    }

    @Override
    public boolean estaVacio() {
        return raiz == null;
    }

    @Override
    public NodoArbolCatalogo getRaiz() {
        return raiz;
    }

    // ------------------------------------------------------------ balanceo

    private int altura(NodoArbolCatalogo nodo) {
        return nodo == null ? 0 : nodo.getAltura();
    }

    private void actualizarAltura(NodoArbolCatalogo nodo) {
        nodo.setAltura(1 + Math.max(altura(nodo.getIzquierdo()), altura(nodo.getDerecho())));
    }

    private int factorBalance(NodoArbolCatalogo nodo) {
        return altura(nodo.getIzquierdo()) - altura(nodo.getDerecho());
    }

    /** Recalcula la altura del nodo y aplica la rotación necesaria si quedó desbalanceado. */
    private NodoArbolCatalogo balancear(NodoArbolCatalogo nodo) {
        actualizarAltura(nodo);
        int factor = factorBalance(nodo);

        if (factor > 1) {
            // Pesa a la izquierda. Si el hijo izquierdo pesa a la derecha es el caso
            // izquierda-derecha: primero se rota ese hijo (rotación doble).
            if (factorBalance(nodo.getIzquierdo()) < 0) {
                nodo.setIzquierdo(rotarIzquierda(nodo.getIzquierdo()));
            }
            return rotarDerecha(nodo);
        }
        if (factor < -1) {
            // Pesa a la derecha. Caso derecha-izquierda: rotación doble.
            if (factorBalance(nodo.getDerecho()) > 0) {
                nodo.setDerecho(rotarDerecha(nodo.getDerecho()));
            }
            return rotarIzquierda(nodo);
        }
        return nodo;
    }

    /** El hijo izquierdo sube y el nodo baja a la derecha. */
    private NodoArbolCatalogo rotarDerecha(NodoArbolCatalogo nodo) {
        NodoArbolCatalogo nuevaRaiz = nodo.getIzquierdo();
        NodoArbolCatalogo subarbolMovido = nuevaRaiz.getDerecho();
        nuevaRaiz.setDerecho(nodo);
        nodo.setIzquierdo(subarbolMovido);
        actualizarAltura(nodo);
        actualizarAltura(nuevaRaiz);
        return nuevaRaiz;
    }

    /** El hijo derecho sube y el nodo baja a la izquierda. */
    private NodoArbolCatalogo rotarIzquierda(NodoArbolCatalogo nodo) {
        NodoArbolCatalogo nuevaRaiz = nodo.getDerecho();
        NodoArbolCatalogo subarbolMovido = nuevaRaiz.getIzquierdo();
        nuevaRaiz.setIzquierdo(nodo);
        nodo.setDerecho(subarbolMovido);
        actualizarAltura(nodo);
        actualizarAltura(nuevaRaiz);
        return nuevaRaiz;
    }
}
