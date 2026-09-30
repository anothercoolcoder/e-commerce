package uptc.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uptc.controller.tree.BPlusTree;

class BPlusTreeTest {

    /** Crea un árbol con las claves 1..cantidad insertadas en orden; el valor de la clave n es "vn". */
    private BPlusTree<Integer, String> arbolCon(int orden, int cantidad) {
        BPlusTree<Integer, String> arbol = new BPlusTree<>(orden);
        for (int clave = 1; clave <= cantidad; clave++) {
            arbol.insert(clave, "v" + clave);
        }
        return arbol;
    }

    // ------------------------------------------------------------ inserción y búsqueda

    @Test
    void insertaYBuscaClavesAunqueElArbolSeDividaVariasVeces() {
        BPlusTree<Integer, String> arbol = arbolCon(3, 20);

        assertEquals(20, arbol.size());
        assertEquals("v1", arbol.search(1));
        assertEquals("v13", arbol.search(13));
        assertEquals("v20", arbol.search(20));
    }

    @Test
    void laHojaLlenaSeDivideYLaRaizGuardaElSeparador() {
        BPlusTree<Integer, String> arbol = new BPlusTree<>(3);
        arbol.insert(1, "v1");
        arbol.insert(2, "v2");
        // Con dos claves todo cabe en una sola hoja, que además es la raíz.
        assertEquals(List.of(1, 2), arbol.rootSeparators());

        arbol.insert(3, "v3");

        // La tercera clave llena la hoja: se divide en [1] y [2, 3] y la nueva raíz guarda el separador 2.
        assertEquals(List.of(2), arbol.rootSeparators());
        assertEquals(List.of("v1", "v2", "v3"), arbol.values());
    }

    @Test
    void insertarUnaClaveExistenteReemplazaElValorSinDuplicarla() {
        BPlusTree<Integer, String> arbol = new BPlusTree<>(3);
        arbol.insert(1, "uno");

        arbol.insert(1, "actualizado");

        assertEquals(1, arbol.size());
        assertEquals("actualizado", arbol.search(1));
    }

    @Test
    void valuesRecorreLasHojasEnOrdenAunqueSeInserteDesordenado() {
        BPlusTree<Integer, String> arbol = new BPlusTree<>(4);
        for (int clave : new int[] {7, 3, 9, 1, 5, 8, 2, 6, 4}) {
            arbol.insert(clave, "v" + clave);
        }

        assertEquals(List.of("v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9"), arbol.values());
    }

    @Test
    void buscaRapidoEntreDiezMilClaves() {
        BPlusTree<Integer, Integer> arbol = new BPlusTree<>(32);
        for (int clave = 0; clave < 10_000; clave++) {
            arbol.insert(clave, clave * 2);
        }

        assertEquals(10_000, arbol.size());
        for (int clave = 0; clave < 10_000; clave++) {
            assertEquals(clave * 2, arbol.search(clave));
        }
    }

    // ------------------------------------------------------------ rangos

    @Test
    void rangoDevuelveLosValoresEntreLosLimitesInclusive() {
        BPlusTree<Integer, String> arbol = arbolCon(3, 20);

        assertEquals(List.of("v5", "v6", "v7", "v8"), arbol.range(5, 8));
    }

    @Test
    void rangoQueSeSaleDeLasClavesSoloDevuelveLasExistentes() {
        BPlusTree<Integer, String> arbol = arbolCon(3, 5);

        assertEquals(List.of("v4", "v5"), arbol.range(4, 100));
        assertEquals(List.of("v1", "v2"), arbol.range(-10, 2));
        assertTrue(arbol.range(50, 60).isEmpty());
    }

    @Test
    void rangoConLimitesInvertidosONulosEsVacio() {
        BPlusTree<Integer, String> arbol = arbolCon(3, 5);

        assertTrue(arbol.range(4, 2).isEmpty());
        assertTrue(arbol.range(null, 2).isEmpty());
        assertTrue(arbol.range(1, null).isEmpty());
    }

    // ------------------------------------------------------------ eliminación

    @Test
    void eliminaClavesPidiendoPrestadoOFusionandoNodos() {
        BPlusTree<Integer, String> arbol = arbolCon(4, 30);

        for (int clave = 1; clave <= 30; clave += 2) {
            assertEquals("v" + clave, arbol.delete(clave));
        }

        assertEquals(15, arbol.size());
        assertNull(arbol.search(1));
        assertEquals("v30", arbol.search(30));
        assertEquals(List.of("v2", "v4", "v6"), arbol.range(1, 6));
    }

    @Test
    void eliminarUnaClaveInexistenteDevuelveNull() {
        BPlusTree<Integer, String> arbol = arbolCon(3, 5);

        assertNull(arbol.delete(99));
        assertNull(arbol.delete(null));
        assertEquals(5, arbol.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 4, 5, 6, 7})
    void eliminarTodasLasClavesEnDesordenDejaElArbolVacio(int orden) {
        BPlusTree<Integer, String> arbol = arbolCon(orden, 200);
        List<Integer> claves = new ArrayList<>();
        for (int clave = 1; clave <= 200; clave++) {
            claves.add(clave);
        }
        Collections.shuffle(claves, new Random(7));

        for (int i = 0; i < claves.size(); i++) {
            assertEquals("v" + claves.get(i), arbol.delete(claves.get(i)));
            assertEquals(200 - i - 1, arbol.values().size());
        }

        assertTrue(arbol.isEmpty());
        assertTrue(arbol.rootSeparators().isEmpty());
    }

    // ------------------------------------------------------------ casos límite

    @Test
    void arbolVacioNoEncuentraNada() {
        BPlusTree<Integer, String> arbol = new BPlusTree<>(3);

        assertTrue(arbol.isEmpty());
        assertEquals(0, arbol.size());
        assertNull(arbol.search(1));
        assertNull(arbol.delete(1));
        assertTrue(arbol.range(1, 10).isEmpty());
        assertTrue(arbol.values().isEmpty());
    }

    @Test
    void elOrdenMinimoEsTres() {
        assertThrows(IllegalArgumentException.class, () -> new BPlusTree<Integer, String>(2));
        assertEquals(3, new BPlusTree<Integer, String>(3).getOrder());
    }

    @Test
    void noAceptaClavesNiValoresNulos() {
        BPlusTree<Integer, String> arbol = new BPlusTree<>(3);

        assertThrows(NullPointerException.class, () -> arbol.insert(null, "valor"));
        assertThrows(NullPointerException.class, () -> arbol.insert(1, null));
        assertNull(arbol.search(null));
    }
}
