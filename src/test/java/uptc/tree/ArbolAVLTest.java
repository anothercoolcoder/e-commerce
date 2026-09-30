package uptc.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import uptc.DatosDePrueba;
import uptc.model.Producto;
import uptc.structures.catalogo.ArbolAVL;
import uptc.structures.catalogo.NodoArbolCatalogo;

class ArbolAVLTest {

    /** Crea un árbol insertando los ids en el orden indicado. */
    private ArbolAVL arbolCon(String... ids) {
        ArbolAVL arbol = new ArbolAVL();
        for (String id : ids) {
            arbol.insertar(DatosDePrueba.producto(id, "Producto " + id, "cat", "Marca", 100, 5));
        }
        return arbol;
    }

    private List<String> idsEnOrden(ArbolAVL arbol) {
        List<String> ids = new ArrayList<>();
        for (Producto producto : arbol.obtenerInOrder()) {
            ids.add(producto.getId());
        }
        return ids;
    }

    /** Comprueba que el árbol quedó con "2" en la raíz, "1" a la izquierda y "3" a la derecha. */
    private void assertBalanceadoEnDos(ArbolAVL arbol) {
        NodoArbolCatalogo raiz = arbol.getRaiz();
        assertEquals("2", raiz.getProducto().getId());
        assertEquals("1", raiz.getIzquierdo().getProducto().getId());
        assertEquals("3", raiz.getDerecho().getProducto().getId());
        assertEquals(2, raiz.getAltura());
    }

    // ------------------------------------------------------------ insertar y buscar

    @Test
    void arbolNuevoEstaVacio() {
        ArbolAVL arbol = new ArbolAVL();

        assertTrue(arbol.estaVacio());
        assertEquals(0, arbol.getTamano());
        assertNull(arbol.getRaiz());
        assertTrue(arbol.obtenerInOrder().isEmpty());
    }

    @Test
    void insertaYBuscaPorId() {
        ArbolAVL arbol = arbolCon("5", "3", "8");

        assertEquals(3, arbol.getTamano());
        assertFalse(arbol.estaVacio());
        assertEquals("Producto 8", arbol.buscar("8").orElseThrow().getNombre());
    }

    @Test
    void buscarUnIdInexistenteONuloDevuelveVacio() {
        ArbolAVL arbol = arbolCon("5", "3", "8");

        assertTrue(arbol.buscar("99").isEmpty());
        assertTrue(arbol.buscar(null).isEmpty());
    }

    @Test
    void insertarUnIdRepetidoReemplazaElProductoSinAumentarElTamano() {
        ArbolAVL arbol = arbolCon("5");

        arbol.insertar(DatosDePrueba.producto("5", "Actualizado", "cat", "Marca", 100, 5));

        assertEquals(1, arbol.getTamano());
        assertEquals("Actualizado", arbol.buscar("5").orElseThrow().getNombre());
    }

    @Test
    void insertarProductoNuloOSinIdLanzaExcepcion() {
        ArbolAVL arbol = new ArbolAVL();

        assertThrows(IllegalArgumentException.class, () -> arbol.insertar(null));
        assertThrows(IllegalArgumentException.class, () -> arbol.insertar(new Producto()));
    }

    @Test
    void recorridoInOrderDevuelveLosIdsOrdenados() {
        ArbolAVL arbol = arbolCon("4", "2", "6", "1", "3", "5", "7");

        assertEquals(List.of("1", "2", "3", "4", "5", "6", "7"), idsEnOrden(arbol));
    }

    // ------------------------------------------------------------ rotaciones

    @Test
    void insertarEnOrdenDescendenteProvocaRotacionSimpleALaDerecha() {
        assertBalanceadoEnDos(arbolCon("3", "2", "1"));
    }

    @Test
    void insertarEnOrdenAscendenteProvocaRotacionSimpleALaIzquierda() {
        assertBalanceadoEnDos(arbolCon("1", "2", "3"));
    }

    @Test
    void casoIzquierdaDerechaProvocaRotacionDoble() {
        assertBalanceadoEnDos(arbolCon("3", "1", "2"));
    }

    @Test
    void casoDerechaIzquierdaProvocaRotacionDoble() {
        assertBalanceadoEnDos(arbolCon("1", "3", "2"));
    }

    @Test
    void laAlturaSeMantieneLogaritmicaConMilInsercionesOrdenadas() {
        ArbolAVL arbol = new ArbolAVL();
        for (int i = 0; i < 1000; i++) {
            String id = String.format("%04d", i);
            arbol.insertar(DatosDePrueba.producto(id, "Producto " + id, "cat", "Marca", 100, 5));
        }

        // Un árbol binario sin balancear tendría altura 1000; un AVL con 1000 nodos no pasa de 14.
        assertEquals(1000, arbol.getTamano());
        assertTrue(arbol.getRaiz().getAltura() <= 14, "altura: " + arbol.getRaiz().getAltura());
        assertEquals("0999", arbol.buscar("0999").orElseThrow().getId());
    }

    // ------------------------------------------------------------ eliminar

    @Test
    void eliminaUnaHoja() {
        ArbolAVL arbol = arbolCon("2", "1", "3");

        assertTrue(arbol.eliminar("1"));

        assertEquals(List.of("2", "3"), idsEnOrden(arbol));
        assertEquals(2, arbol.getTamano());
    }

    @Test
    void eliminaUnNodoConUnSoloHijo() {
        ArbolAVL arbol = arbolCon("2", "1", "3", "4");

        assertTrue(arbol.eliminar("3"));

        assertEquals(List.of("1", "2", "4"), idsEnOrden(arbol));
    }

    @Test
    void eliminaUnNodoConDosHijosUsandoSuSucesor() {
        ArbolAVL arbol = arbolCon("4", "2", "6", "1", "3", "5", "7");

        assertTrue(arbol.eliminar("4"));

        // El sucesor de "4" (el menor del subárbol derecho) es "5" y ocupa la raíz.
        assertEquals("5", arbol.getRaiz().getProducto().getId());
        assertEquals(List.of("1", "2", "3", "5", "6", "7"), idsEnOrden(arbol));
    }

    @Test
    void eliminarRebalanceaElArbol() {
        ArbolAVL arbol = arbolCon("2", "1", "3", "4");

        arbol.eliminar("1");

        // Sin "1" el árbol pesa a la derecha (2 → 3 → 4) y una rotación deja "3" en la raíz.
        assertEquals("3", arbol.getRaiz().getProducto().getId());
        assertEquals(2, arbol.getRaiz().getAltura());
    }

    @Test
    void eliminarUnIdInexistenteDevuelveFalseYNoCambiaElArbol() {
        ArbolAVL arbol = arbolCon("2", "1", "3");

        assertFalse(arbol.eliminar("99"));
        assertFalse(arbol.eliminar(null));
        assertEquals(3, arbol.getTamano());
    }

    @Test
    void eliminarElUnicoNodoDejaElArbolVacio() {
        ArbolAVL arbol = arbolCon("1");

        assertTrue(arbol.eliminar("1"));

        assertTrue(arbol.estaVacio());
        assertEquals(0, arbol.getTamano());
    }

    @Test
    void vaciarQuitaTodosLosProductos() {
        ArbolAVL arbol = arbolCon("2", "1", "3");

        arbol.vaciar();

        assertTrue(arbol.estaVacio());
        assertTrue(arbol.buscar("2").isEmpty());
    }

    // ------------------------------------------------------------ consulta por precio

    @Test
    void buscaPorRangoDePrecioIncluyendoLosLimites() {
        ArbolAVL arbol = new ArbolAVL();
        arbol.insertar(DatosDePrueba.producto("1", "Barato", "cat", "Marca", 50, 5));
        arbol.insertar(DatosDePrueba.producto("2", "Medio", "cat", "Marca", 100, 5));
        arbol.insertar(DatosDePrueba.producto("3", "Caro", "cat", "Marca", 900, 5));

        List<Producto> resultado = arbol.buscarPorRangoPrecio(50, 100);

        assertEquals(2, resultado.size());
        assertEquals("Barato", resultado.get(0).getNombre());
        assertEquals("Medio", resultado.get(1).getNombre());
    }
}
