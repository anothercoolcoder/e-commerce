package uptc.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uptc.DatosDePrueba.ADMIN;
import static uptc.DatosDePrueba.CLIENTE;
import static uptc.DatosDePrueba.producto;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uptc.DatosDePrueba;
import uptc.exception.CarritoVacioException;
import uptc.exception.StockInsuficienteException;
import uptc.exception.ValidationException;
import uptc.model.Compra;
import uptc.model.Producto;
import uptc.persistence.CompraJsonDao;

class CompraControllerTest {
    @TempDir
    Path carpeta;

    private ProductoController catalogo;
    private CarritoController carrito;
    private CompraController compras;

    @BeforeEach
    void prepararTienda() throws Exception {
        Producto lampara = producto("lampara", "Lámpara", "Hogar", "Luma", 100_000, 5);
        lampara.setDescuento(10);
        catalogo = DatosDePrueba.catalogo(carpeta, lampara, producto("taza", "Taza", "Cocina", "Casa", 20_000, 3));
        carrito = new CarritoController(catalogo);
        compras = new CompraController(catalogo, new CompraJsonDao(carpeta.resolve("compras.json")));
    }

    @Test
    void checkoutRegistraElPedidoDescuentaStockYVaciaElCarrito() throws Exception {
        carrito.add("lampara", 2);

        Compra pedido = compras.checkout("ana", carrito);

        assertNotNull(pedido.getId());
        assertEquals("ana", pedido.getUsuarioId());
        assertEquals(3, catalogo.find("lampara").getStock());
        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void elPedidoGuardaElPrecioPagadoYElTotalConImpuestoYEnvio() throws Exception {
        carrito.add("lampara", 2);

        Compra pedido = compras.checkout("ana", carrito);

        // 2 × 90.000 (precio con 10 % de descuento) = 180.000; + 19 % de IVA + 12.000 de envío.
        assertEquals(90_000, pedido.getDetalles().get(0).getPrecioUnitario(), 0.01);
        assertEquals(2, pedido.getDetalles().get(0).getCantidad());
        assertEquals(180_000 * 1.19 + 12_000, pedido.getTotal(), 0.01);
    }

    @Test
    void elPedidoQuedaPersistidoEnElJson() throws Exception {
        carrito.add("taza", 1);
        compras.checkout("ana", carrito);

        List<Compra> guardadas = new CompraJsonDao(carpeta.resolve("compras.json")).load();

        assertEquals(1, guardadas.size());
        assertEquals("taza", guardadas.get(0).getDetalles().get(0).getProductoId());
        assertNotNull(guardadas.get(0).getFecha());
    }

    @Test
    void noSePuedeComprarUnCarritoVacio() {
        assertThrows(CarritoVacioException.class, () -> compras.checkout("ana", carrito));
    }

    @Test
    void siElStockBajoDespuesDeAgregarAlCarritoLaCompraSeRechazaCompleta() throws Exception {
        carrito.add("lampara", 1);
        carrito.add("taza", 3);
        // Mientras el cliente compraba, el administrador redujo el stock de tazas.
        Producto taza = catalogo.find("taza");
        taza.setStock(1);
        catalogo.update(ADMIN, taza);

        assertThrows(StockInsuficienteException.class, () -> compras.checkout("ana", carrito));

        // No se descontó nada ni se registró el pedido.
        assertEquals(5, catalogo.find("lampara").getStock());
        assertTrue(compras.all().isEmpty());
        assertEquals(2, carrito.getItems().size());
    }

    @Test
    void elAdminPuedeVerElHistorialDeCualquierUsuario() throws Exception {
        carrito.add("taza", 2);
        compras.checkout("cliente", carrito);

        List<Compra> historial = compras.historyFor(ADMIN, "cliente");

        assertEquals(1, historial.size());
        assertEquals("taza", historial.get(0).getDetalles().get(0).getProductoId());
        assertEquals(2, historial.get(0).getDetalles().get(0).getCantidad());
    }

    @Test
    void unClienteSoloPuedeVerSuPropioHistorial() throws Exception {
        carrito.add("taza", 1);
        compras.checkout("cliente", carrito);

        assertEquals(1, compras.historyFor(CLIENTE, "cliente").size());
        assertThrows(ValidationException.class, () -> compras.historyFor(CLIENTE, "ana"));
        assertThrows(ValidationException.class, () -> compras.historyFor(null, "ana"));
    }

    @Test
    void cadaUsuarioTieneSuPropioHistorial() throws Exception {
        carrito.add("taza", 1);
        compras.checkout("ana", carrito);
        carrito.add("taza", 1);
        compras.checkout("ana", carrito);
        carrito.add("lampara", 1);
        compras.checkout("luis", carrito);

        assertEquals(2, compras.history("ana").size());
        assertEquals(1, compras.history("luis").size());
        assertTrue(compras.history("nadie").isEmpty());
        assertEquals(3, compras.all().size());
    }
}
