package uptc.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uptc.DatosDePrueba.producto;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uptc.DatosDePrueba;
import uptc.model.Compra;
import uptc.model.DetalleCompra;
import uptc.model.Producto;

class EstadisticasControllerTest {
    private final EstadisticasController estadisticas = new EstadisticasController();

    private Compra compra(double total, DetalleCompra... detalles) {
        return new Compra("c", "ana", LocalDateTime.now(), List.of(detalles), total);
    }

    @Test
    void sumaLasVentasYCalculaElTicketPromedio() {
        List<Compra> compras = List.of(compra(100_000), compra(50_000));

        assertEquals(150_000, estadisticas.totalSales(compras));
        assertEquals(75_000, estadisticas.averageTicket(compras));
    }

    @Test
    void sinComprasLasVentasYElTicketSonCero() {
        assertEquals(0, estadisticas.totalSales(List.of()));
        assertEquals(0, estadisticas.averageTicket(List.of()));
    }

    @Test
    void cuentaLasUnidadesVendidasEnTotalYPorProducto() {
        List<Compra> compras = List.of(
                compra(0, new DetalleCompra("taza", 2, 20_000), new DetalleCompra("laptop", 1, 3_000_000)),
                compra(0, new DetalleCompra("taza", 3, 20_000)));

        assertEquals(6, estadisticas.unitsSold(compras));
        assertEquals(Map.of("taza", 5, "laptop", 1), estadisticas.unitsByProduct(compras));
    }

    @Test
    void ordenaLosProductosMasVendidosDeMayorAMenor() {
        List<Compra> compras = List.of(
                compra(0, new DetalleCompra("olla", 1, 90_000), new DetalleCompra("taza", 5, 20_000)),
                compra(0, new DetalleCompra("laptop", 3, 3_000_000)));

        assertEquals(List.of("taza", "laptop", "olla"), estadisticas.topProducts(compras, 5));
        assertEquals(List.of("taza"), estadisticas.topProducts(compras, 1));
    }

    @Test
    void sumaLasVentasDeCadaUsuario() {
        List<Compra> compras = List.of(
                new Compra("c1", "ana", LocalDateTime.now(), List.of(), 100_000),
                new Compra("c2", "luis", LocalDateTime.now(), List.of(), 30_000),
                new Compra("c3", "ana", LocalDateTime.now(), List.of(), 50_000));

        assertEquals(Map.of("ana", 150_000.0, "luis", 30_000.0), estadisticas.salesByUser(compras));
    }

    @Test
    void listaLosProductosActivosConStockBajoEmpezandoPorElMenor(@TempDir Path carpeta) throws Exception {
        ProductoController catalogo = DatosDePrueba.catalogo(carpeta,
                producto("taza", "Taza", "Cocina", "Casa", 20_000, 4),
                producto("olla", "Olla", "Cocina", "Casa", 90_000, 0),
                producto("laptop", "Laptop", "Computadores", "Nova", 3_000_000, 50),
                producto("mouse", "Mouse", "Computadores", "Nova", 60_000, 1));
        catalogo.deactivate(DatosDePrueba.ADMIN, "mouse"); // un producto desactivado no hay que reponerlo

        List<Producto> stockBajo = estadisticas.lowStock(catalogo, 5);

        assertEquals(2, stockBajo.size());
        assertEquals("Olla", stockBajo.get(0).getNombre());
        assertEquals("Taza", stockBajo.get(1).getNombre());
    }

    @Test
    void cuentaLasUnidadesVendidasPorCategoria(@TempDir Path carpeta) throws Exception {
        ProductoController catalogo = DatosDePrueba.catalogo(carpeta,
                producto("taza", "Taza", "Cocina", "Casa", 20_000, 5),
                producto("olla", "Olla", "Cocina", "Casa", 90_000, 5),
                producto("laptop", "Laptop", "Computadores", "Nova", 3_000_000, 5));
        List<Compra> compras = List.of(
                compra(0, new DetalleCompra("taza", 2, 20_000), new DetalleCompra("laptop", 1, 3_000_000)),
                compra(0, new DetalleCompra("olla", 3, 90_000), new DetalleCompra("descatalogado", 9, 1)));

        Map<String, Integer> unidades = estadisticas.salesByCategory(compras, catalogo);

        // El detalle de un producto que ya no existe se omite.
        assertEquals(Map.of("Cocina", 5, "Computadores", 1), unidades);
    }
}
