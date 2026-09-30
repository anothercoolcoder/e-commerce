package uptc.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uptc.DatosDePrueba.ADMIN;
import static uptc.DatosDePrueba.producto;

import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uptc.DatosDePrueba;
import uptc.exception.ProductoNoEncontradoException;
import uptc.exception.StockInsuficienteException;
import uptc.model.Producto;

class CarritoControllerTest {
    @TempDir
    Path carpeta;

    private ProductoController catalogo;
    private CarritoController carrito;

    @BeforeEach
    void crearCarrito() throws Exception {
        Producto taza = producto("taza", "Taza", "Cocina", "Casa", 20_000, 5);
        Producto lampara = producto("lampara", "Lámpara", "Hogar", "Luma", 100_000, 2);
        lampara.setDescuento(10);
        catalogo = DatosDePrueba.catalogo(carpeta, taza, lampara);
        carrito = new CarritoController(catalogo);
    }

    // ------------------------------------------------------------ agregar

    @Test
    void agregaUnProductoAlCarrito() {
        carrito.add("taza", 2);

        assertEquals(1, carrito.getItems().size());
        assertEquals(2, carrito.getItems().get(0).getCantidad());
    }

    @Test
    void agregarElMismoProductoSumaLaCantidadEnUnaSolaLinea() {
        carrito.add("taza", 2);
        carrito.add("taza", 1);

        assertEquals(1, carrito.getItems().size());
        assertEquals(3, carrito.getItems().get(0).getCantidad());
    }

    @Test
    void rechazaCantidadesCeroONegativas() {
        assertThrows(IllegalArgumentException.class, () -> carrito.add("taza", 0));
        assertThrows(IllegalArgumentException.class, () -> carrito.add("taza", -1));
        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void rechazaMasUnidadesQueElStock() {
        carrito.add("lampara", 2);

        // Ya hay 2 en el carrito y el stock es 2: una más supera lo disponible.
        assertThrows(StockInsuficienteException.class, () -> carrito.add("lampara", 1));
        assertThrows(StockInsuficienteException.class, () -> carrito.add("taza", 6));
    }

    @Test
    void rechazaUnProductoInexistente() {
        assertThrows(ProductoNoEncontradoException.class, () -> carrito.add("no-existe", 1));
    }

    @Test
    void rechazaUnProductoDesactivado() throws Exception {
        catalogo.delete(ADMIN, "taza");

        assertThrows(ProductoNoEncontradoException.class, () -> carrito.add("taza", 1));
    }

    // ------------------------------------------------------------ modificar

    @Test
    void cambiaLaCantidadDeUnaLinea() {
        carrito.add("taza", 1);

        carrito.setQuantity("taza", 4);

        assertEquals(4, carrito.getItems().get(0).getCantidad());
    }

    @Test
    void cantidadCeroEliminaLaLinea() {
        carrito.add("taza", 1);

        carrito.setQuantity("taza", 0);

        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void cambiarCantidadValidaStockSignoYExistencia() {
        carrito.add("taza", 1);

        assertThrows(StockInsuficienteException.class, () -> carrito.setQuantity("taza", 6));
        assertThrows(IllegalArgumentException.class, () -> carrito.setQuantity("taza", -1));
        assertThrows(ProductoNoEncontradoException.class, () -> carrito.setQuantity("lampara", 1));
    }

    @Test
    void eliminaUnaLineaYVaciaElCarrito() {
        carrito.add("taza", 1);
        carrito.add("lampara", 1);

        carrito.remove("taza");
        assertEquals("lampara", carrito.getItems().get(0).getProducto().getId());

        carrito.clear();
        assertTrue(carrito.getItems().isEmpty());
    }

    // ------------------------------------------------------------ totales

    @Test
    void calculaSubtotalImpuestoYTotal() {
        carrito.add("taza", 2);     // 2 × 20.000            = 40.000
        carrito.add("lampara", 1);  // 100.000 con 10 % dto. = 90.000

        assertEquals(130_000, carrito.subtotal(), 0.01);
        assertEquals(24_700, carrito.tax(), 0.01);                    // 19 % de 130.000
        assertEquals(130_000 + 24_700 + 12_000, carrito.total(), 0.01); // + envío
    }

    @Test
    void unCarritoVacioNoCobraEnvio() {
        assertEquals(0, carrito.subtotal());
        assertEquals(0, carrito.tax());
        assertEquals(0, carrito.total());
    }

    // ------------------------------------------------------------ observer

    @Test
    void notificaALosListenersCadaVezQueCambia() {
        int[] avisos = {0};
        PropertyChangeListener listener = evento -> avisos[0]++;
        carrito.addListener(listener);

        carrito.add("taza", 1);
        carrito.setQuantity("taza", 2);
        carrito.remove("taza");
        assertEquals(3, avisos[0]);

        carrito.removeListener(listener);
        carrito.add("taza", 1);
        assertEquals(3, avisos[0]);
    }
}
