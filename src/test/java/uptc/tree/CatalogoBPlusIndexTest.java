package uptc.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uptc.DatosDePrueba;
import uptc.controller.tree.CatalogoBPlusIndex;
import uptc.model.Producto;

class CatalogoBPlusIndexTest {
    private final Producto audifonos = DatosDePrueba.producto("1", "Audífonos Sonix", "Tecnología", "Sonix", 300, 5);
    private final Producto audifonosPro = DatosDePrueba.producto("2", "Audífonos Pro", "Tecnología", "Nova", 900, 5);
    private final Producto taza = DatosDePrueba.producto("3", "Taza térmica", "Cocina", "Casa", 50, 5);
    private final Producto teclado = DatosDePrueba.producto("4", "Teclado mecánico", "Tecnología", "Nova", 300, 5);

    private CatalogoBPlusIndex indice;

    @BeforeEach
    void crearIndice() {
        indice = new CatalogoBPlusIndex(3);
        indice.insert(audifonos);
        indice.insert(audifonosPro);
        indice.insert(taza);
        indice.insert(teclado);
    }

    private List<String> nombres(List<Producto> productos) {
        List<String> nombres = new ArrayList<>();
        for (Producto producto : productos) {
            nombres.add(producto.getNombre());
        }
        return nombres;
    }

    // ------------------------------------------------------------ prefijos

    @Test
    void buscaPorPrefijoSinDistinguirMayusculas() {
        assertEquals(List.of("Audífonos Pro", "Audífonos Sonix"), nombres(indice.findByNamePrefix("AUD")));
    }

    @Test
    void elPrefijoSoloCoincideConElInicioDelNombre() {
        assertEquals(List.of("Taza térmica", "Teclado mecánico"), nombres(indice.findByNamePrefix("t")));
        assertTrue(indice.findByNamePrefix("mecánico").isEmpty());
    }

    @Test
    void prefijoVacioONuloDevuelveTodoElCatalogo() {
        assertEquals(4, indice.findByNamePrefix("").size());
        assertEquals(4, indice.findByNamePrefix(null).size());
    }

    @Test
    void dosProductosConElMismoNombreSeConservanLosDos() {
        indice.insert(DatosDePrueba.producto("5", "Taza térmica", "Cocina", "Otra", 70, 5));

        assertEquals(2, indice.findByNamePrefix("taza").size());
    }

    // ------------------------------------------------------------ rangos de precio

    @Test
    void buscaPorRangoDePrecioIncluyendoLosLimites() {
        assertEquals(List.of("Taza térmica", "Audífonos Sonix", "Teclado mecánico"),
                nombres(indice.findByPriceRange(50, 300)));
    }

    @Test
    void rangoDePrecioSinProductosOConLimitesInvertidosEsVacio() {
        assertTrue(indice.findByPriceRange(1000, 2000).isEmpty());
        assertTrue(indice.findByPriceRange(300, 50).isEmpty());
    }

    // ------------------------------------------------------------ mantenimiento

    @Test
    void eliminarQuitaElProductoDeLosDosIndices() {
        assertTrue(indice.delete(taza));

        assertTrue(indice.findByNamePrefix("taza").isEmpty());
        assertTrue(indice.findByPriceRange(50, 50).isEmpty());
        assertEquals(3, indice.all().size());
    }

    @Test
    void eliminarUnProductoQueNoEstaDevuelveFalse() {
        assertFalse(indice.delete(DatosDePrueba.producto("99", "Inexistente", "cat", "Marca", 10, 1)));
        assertFalse(indice.delete(null));
    }

    @Test
    void allDevuelveLosProductosOrdenadosPorNombre() {
        assertEquals(List.of("Audífonos Pro", "Audífonos Sonix", "Taza térmica", "Teclado mecánico"),
                nombres(indice.all()));
    }

    @Test
    void clearVaciaLosIndices() {
        indice.clear();

        assertTrue(indice.all().isEmpty());
        assertTrue(indice.findByPriceRange(0, 1000).isEmpty());
        assertEquals(3, indice.getOrder());
    }

    @Test
    void insertarUnProductoNuloOSinNombreLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> indice.insert(null));
        assertThrows(IllegalArgumentException.class, () -> indice.insert(new Producto()));
    }
}
