package uptc.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uptc.DatosDePrueba.ADMIN;
import static uptc.DatosDePrueba.producto;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uptc.DatosDePrueba;
import uptc.controller.tree.DecisionContext;
import uptc.controller.tree.DecisionTree;
import uptc.controller.tree.RecommendationProfile;
import uptc.model.Interaccion;
import uptc.model.Producto;
import uptc.model.TipoInteraccion;
import uptc.persistence.CompraJsonDao;

class RecomendacionControllerTest {
    @TempDir
    Path carpeta;

    private ProductoController catalogo;
    private CarritoController carrito;
    private CompraController compras;
    private final List<Interaccion> interacciones = new ArrayList<>();
    private RecomendacionController recomendaciones;

    @BeforeEach
    void prepararTienda() throws Exception {
        Producto tazaEnOferta = producto("taza", "Taza", "Cocina", "Casa", 20_000, 50);
        tazaEnOferta.setDescuento(15);
        Producto olla = producto("olla", "Olla", "Cocina", "Sazón", 150_000, 50);
        Producto horno = producto("horno", "Horno", "Cocina", "Cuisina", 900_000, 50);
        Producto laptop = producto("laptop", "Laptop", "Computadores", "Nova", 3_000_000, 50);
        laptop.setCalificacion(5);
        Producto tablet = producto("tablet", "Tablet", "Computadores", "Nova", 1_500_000, 50);
        Producto monitor = producto("monitor", "Monitor", "Computadores", "Kairo", 800_000, 50);
        Producto agotado = producto("agotado", "Mouse agotado", "Computadores", "Nova", 60_000, 0);
        agotado.setCalificacion(5);

        catalogo = DatosDePrueba.catalogo(carpeta, tazaEnOferta, olla, horno, laptop, tablet, monitor, agotado);
        carrito = new CarritoController(catalogo);
        compras = new CompraController(catalogo, new CompraJsonDao(carpeta.resolve("compras.json")));
        recomendaciones = new RecomendacionController(catalogo, compras, new DecisionTree(), interacciones);
    }

    /** Registra que el usuario hizo clic en un producto. */
    private void clic(String usuarioId, String productoId) {
        interacciones.add(new Interaccion("i" + interacciones.size(), usuarioId, productoId, TipoInteraccion.CLIC,
                LocalDateTime.now(), TipoInteraccion.CLIC.getPeso()));
    }

    /** El usuario compra una unidad del producto. */
    private void comprar(String usuarioId, String productoId) throws Exception {
        carrito.add(productoId, 1);
        compras.checkout(usuarioId, carrito);
    }

    private List<String> ids(List<Producto> productos) {
        List<String> ids = new ArrayList<>();
        for (Producto producto : productos) {
            ids.add(producto.getId());
        }
        return ids;
    }

    // ------------------------------------------------------------ usuario sin historial

    @Test
    void unUsuarioSinHistorialRecibeLosMejorCalificadosDisponibles() {
        List<Producto> recomendados = recomendaciones.forUser("nuevo", 3);

        // La laptop (calificación 5) va primero; el mouse también tiene 5 pero está agotado.
        assertEquals(3, recomendados.size());
        assertEquals("laptop", recomendados.get(0).getId());
        assertTrue(!ids(recomendados).contains("agotado"));
        assertEquals(RecommendationProfile.EXPLORADOR_NUEVO, recomendaciones.profileFor("nuevo"));
    }

    // ------------------------------------------------------------ usuario con historial

    @Test
    void elContextoResumeLaCategoriaFavoritaYElPrecioPromedio() {
        clic("ana", "taza");
        clic("ana", "olla");
        clic("ana", "laptop");

        DecisionContext contexto = recomendaciones.contextFor("ana");

        assertEquals("Cocina", contexto.categoria());
        assertEquals((20_000 + 150_000 + 3_000_000) / 3.0, contexto.precioPromedio(), 0.01);
        assertEquals("", contexto.marca()); // tres marcas distintas: ninguna domina
        assertEquals(0, contexto.compras());
    }

    @Test
    void historialEconomicoRecomiendaProductosBaratosDeLaCategoriaFavorita() {
        clic("ana", "taza");

        // Categoría favorita Cocina y precio promedio bajo: gama económica (el horno de 900.000 queda fuera).
        assertEquals(List.of("taza", "olla"), ids(recomendaciones.forUser("ana", 5)));
        assertEquals(RecommendationProfile.CATEGORIA_ECONOMICA, recomendaciones.profileFor("ana"));
    }

    @Test
    void historialCostosoRecomiendaLaGamaAltaDeLaCategoriaFavorita() {
        clic("luis", "horno");
        clic("luis", "olla");

        // Promedio (900.000 + 150.000) / 2 > 500.000: solo productos de Cocina de más de 500.000.
        assertEquals(List.of("horno"), ids(recomendaciones.forUser("luis", 5)));
        assertEquals(RecommendationProfile.CATEGORIA_PREMIUM, recomendaciones.profileFor("luis"));
    }

    @Test
    void unaMarcaDominanteRecomiendaProductosDeEsaMarca() {
        clic("eva", "laptop");
        clic("eva", "tablet");
        clic("eva", "monitor");

        // Nova concentra 2 de 3 interacciones: se recomiendan productos Nova disponibles.
        assertEquals(List.of("laptop", "tablet"), ids(recomendaciones.forUser("eva", 5)));
        assertEquals(RecommendationProfile.MARCA_PREFERIDA, recomendaciones.profileFor("eva"));
    }

    @Test
    void unCompradorFrecuenteRecibeOfertasDeSuCategoriaYNoLoQueYaCompro() throws Exception {
        comprar("sara", "olla");
        comprar("sara", "olla");
        comprar("sara", "horno");
        comprar("sara", "horno");
        comprar("sara", "laptop");

        // 5 compras, categoría favorita Cocina: solo la taza está en oferta y no la ha comprado.
        assertEquals(5, recomendaciones.contextFor("sara").compras());
        assertEquals(List.of("taza"), ids(recomendaciones.forUser("sara", 5)));
        assertEquals(RecommendationProfile.COMPRADOR_FRECUENTE, recomendaciones.profileFor("sara"));
        assertTrue(recomendaciones.explain("sara").contains(" → "));
    }

    @Test
    void siElPerfilNoEncuentraProductosSeRecomiendanLosMejorCalificados() throws Exception {
        clic("leo", "horno");
        comprar("leo", "horno");

        // Perfil premium en Cocina, pero el único producto premium ya lo compró.
        List<Producto> recomendados = recomendaciones.forUser("leo", 2);

        assertEquals(2, recomendados.size());
        assertEquals("laptop", recomendados.get(0).getId());
    }

    @Test
    void cadaUsuarioRecibeRecomendacionesSegunSuPropioHistorial() {
        clic("ana", "taza");
        clic("eva", "laptop");
        clic("eva", "tablet");

        assertEquals("Cocina", recomendaciones.forUser("ana", 1).get(0).getCategoriaId());
        assertEquals("Computadores", recomendaciones.forUser("eva", 1).get(0).getCategoriaId());
    }

    @Test
    void ignoraInteraccionesConProductosQueYaNoExisten() {
        clic("ana", "producto-borrado");

        assertEquals("", recomendaciones.contextFor("ana").categoria());
    }

    @Test
    void respetaElLimiteDeResultados() {
        assertEquals(2, recomendaciones.forUser("nuevo", 2).size());
        assertTrue(recomendaciones.forUser("nuevo", 0).isEmpty());
    }

    @Test
    void noRecomiendaProductosDesactivados() throws Exception {
        catalogo.deactivate(ADMIN, "laptop");

        assertTrue(!ids(recomendaciones.forUser("nuevo", 10)).contains("laptop"));
    }
}
