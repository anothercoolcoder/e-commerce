package uptc.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import uptc.exception.ProductoNoEncontradoException;
import uptc.exception.StockInsuficienteException;
import uptc.exception.ValidationException;
import uptc.model.Producto;
import uptc.persistence.ProductoCsvDao;

class ProductoControllerTest {
    @TempDir
    Path carpeta;

    private ProductoController catalogo;

    @BeforeEach
    void crearCatalogo() throws Exception {
        catalogo = DatosDePrueba.catalogo(carpeta,
                producto("1", "Laptop Pro", "Computadores", "Nova", 3_000_000, 4),
                producto("2", "Laptop Básica", "Computadores", "Kairo", 1_200_000, 0),
                producto("3", "Taza térmica", "Cocina", "Casa", 50_000, 10));
    }

    // ------------------------------------------------------------ crear

    @Test
    void crearUnProductoValidoLoGuardaEnElCsv() throws Exception {
        catalogo.create(ADMIN, producto("4", "Mouse", "Tecnología", "Nova", 80_000, 7));

        // Un DAO nuevo lee el archivo: comprueba que el producto quedó persistido.
        List<Producto> guardados = new ProductoCsvDao(carpeta.resolve("productos.csv")).load();
        assertEquals(4, guardados.size());
        assertEquals("Mouse", guardados.get(3).getNombre());
    }

    @Test
    void crearRechazaUnProductoInvalido() {
        Producto sinNombre = producto("4", "", "Cocina", "Casa", 100, 1);
        Producto precioCero = producto("4", "Vaso", "Cocina", "Casa", 0, 1);

        assertThrows(ValidationException.class, () -> catalogo.create(ADMIN, sinNombre));
        assertThrows(ValidationException.class, () -> catalogo.create(ADMIN, precioCero));
        assertEquals(3, catalogo.findAll().size());
    }

    @Test
    void crearRechazaIdOSkuRepetidos() {
        Producto mismoId = producto("1", "Otro", "Cocina", "Casa", 100, 1);
        Producto mismoSku = producto("9", "Otro", "Cocina", "Casa", 100, 1);
        mismoSku.setSku("sku-3");

        assertThrows(ValidationException.class, () -> catalogo.create(ADMIN, mismoId));
        assertThrows(ValidationException.class, () -> catalogo.create(ADMIN, mismoSku));
    }

    // ------------------------------------------------------------ buscar

    @Test
    void buscaUnProductoPorId() {
        assertEquals("Taza térmica", catalogo.find("3").getNombre());
        assertTrue(catalogo.exists("3"));
    }

    @Test
    void buscarUnIdInexistenteLanzaExcepcion() {
        assertFalse(catalogo.exists("99"));
        assertThrows(ProductoNoEncontradoException.class, () -> catalogo.find("99"));
    }

    @Test
    void buscaPorPrefijoDeNombreYPorRangoDePrecio() {
        assertEquals(2, catalogo.searchPrefix("lap").size());
        assertEquals("Taza térmica", catalogo.searchPrice(0, 100_000).get(0).getNombre());
    }

    @Test
    void filtraPorTextoCategoriaMarcaYDisponibilidad() {
        assertEquals(2, catalogo.filter("laptop", null, null, false).size());
        assertEquals(1, catalogo.filter("laptop", null, null, true).size());
        assertEquals(1, catalogo.filter("", "Cocina", null, false).size());
        assertEquals("Laptop Básica", catalogo.filter(null, "Computadores", "kairo", false).get(0).getNombre());
    }

    @Test
    void listaLasCategoriasDelCatalogoSinRepetir() {
        assertEquals(List.of("Computadores", "Cocina"), catalogo.categoryNames());
    }

    // ------------------------------------------------------------ actualizar

    @Test
    void actualizarCambiaLosDatosYMantieneLosIndicesAlDia() throws Exception {
        Producto laptop = catalogo.find("1");
        laptop.setNombre("Portátil Pro");
        laptop.setPrecio(2_500_000);

        catalogo.update(ADMIN, laptop);

        assertEquals("Portátil Pro", catalogo.find("1").getNombre());
        assertEquals(1, catalogo.searchPrefix("portátil").size());
        assertEquals(1, catalogo.searchPrefix("laptop").size());
        assertEquals(1, catalogo.searchPrice(2_500_000, 2_500_000).size());
        assertTrue(catalogo.searchPrice(3_000_000, 3_000_000).isEmpty());
    }

    @Test
    void actualizarRechazaUnProductoInexistenteOInvalido() {
        Producto inexistente = producto("99", "Nada", "Cocina", "Casa", 100, 1);
        Producto stockNegativo = producto("3", "Taza térmica", "Cocina", "Casa", 50_000, -1);

        assertThrows(ProductoNoEncontradoException.class, () -> catalogo.update(ADMIN, inexistente));
        assertThrows(ValidationException.class, () -> catalogo.update(ADMIN, stockNegativo));
    }

    // ------------------------------------------------------------ eliminar / desactivar

    @Test
    void eliminarDesactivaElProductoYLoOcultaDeLaTienda() throws Exception {
        catalogo.delete(ADMIN, "3");

        // Sigue existiendo (las compras antiguas lo referencian) pero ya no sale en la búsqueda.
        assertFalse(catalogo.find("3").isActivo());
        assertTrue(catalogo.filter("taza", null, null, false).isEmpty());
        assertFalse(new ProductoCsvDao(carpeta.resolve("productos.csv")).load().get(2).isActivo());
    }

    // ------------------------------------------------------------ stock

    @Test
    void descontarStockRestaLasUnidadesVendidas() throws Exception {
        catalogo.reduceStock("3", 4);

        assertEquals(6, catalogo.find("3").getStock());
    }

    @Test
    void noSePuedeDescontarMasStockDelDisponible() {
        assertThrows(StockInsuficienteException.class, () -> catalogo.reduceStock("3", 11));
        assertThrows(StockInsuficienteException.class, () -> catalogo.reduceStock("3", 0));
        assertEquals(10, catalogo.find("3").getStock());
    }

    // ------------------------------------------------------------ roles

    @Test
    void soloUnAdminPuedeCrearActualizarOEliminar() {
        Producto nuevo = producto("4", "Mouse", "Tecnología", "Nova", 80_000, 7);

        assertThrows(ValidationException.class, () -> catalogo.create(CLIENTE, nuevo));
        assertThrows(ValidationException.class, () -> catalogo.update(CLIENTE, catalogo.find("1")));
        assertThrows(ValidationException.class, () -> catalogo.delete(CLIENTE, "1"));
        assertThrows(ValidationException.class, () -> catalogo.create(null, nuevo));
        assertEquals(3, catalogo.findAll().size());
        assertTrue(catalogo.find("1").isActivo());
    }

    @Test
    void recargarVuelveALeerElArchivo() throws Exception {
        ProductoCsvDao dao = new ProductoCsvDao(carpeta.resolve("productos.csv"));
        dao.save(List.of(producto("7", "Único", "Cocina", "Casa", 10, 1)));

        catalogo.reload();

        assertEquals(1, catalogo.findAll().size());
        assertTrue(catalogo.exists("7"));
        assertFalse(catalogo.exists("1"));
    }
}
