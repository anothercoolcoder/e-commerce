package uptc.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uptc.exception.PersistenceException;
import uptc.model.Compra;
import uptc.model.DetalleCompra;
import uptc.model.Producto;
import uptc.model.RolUsuario;
import uptc.model.Usuario;

/** Pruebas de los DAO. Cada prueba escribe en una carpeta temporal que JUnit borra al terminar. */
class PersistenceDaoTest {
    @TempDir
    Path carpeta;

    // ------------------------------------------------------------ productos (CSV)

    @Test
    void csvGuardaYRecuperaTodosLosCamposDelProducto() throws Exception {
        ProductoCsvDao dao = new ProductoCsvDao(carpeta.resolve("productos.csv"));
        Producto original = new Producto("1", "SKU-1", "Mouse", "Inalámbrico", 100, 10, 3, "Tecnología", "Nova", 4.5, "mouse.png", false);

        dao.save(List.of(original));
        Producto leido = dao.load().get(0);

        assertEquals("1", leido.getId());
        assertEquals("SKU-1", leido.getSku());
        assertEquals("Mouse", leido.getNombre());
        assertEquals("Inalámbrico", leido.getDescripcion());
        assertEquals(100, leido.getPrecio());
        assertEquals(10, leido.getDescuento());
        assertEquals(3, leido.getStock());
        assertEquals("Tecnología", leido.getCategoriaId());
        assertEquals("Nova", leido.getMarca());
        assertEquals(4.5, leido.getCalificacion());
        assertEquals("mouse.png", leido.getImagen());
        assertFalse(leido.isActivo());
    }

    @Test
    void csvConservaComasYComillasDentroDeUnCampo() throws Exception {
        ProductoCsvDao dao = new ProductoCsvDao(carpeta.resolve("productos.csv"));
        Producto producto = new Producto("1", "SKU-1", "Mouse, inalámbrico", "Pantalla de 27\" curva", 100, 0, 3, "tec", "Marca", 4, "", true);

        dao.save(List.of(producto));
        Producto leido = dao.load().get(0);

        assertEquals("Mouse, inalámbrico", leido.getNombre());
        assertEquals("Pantalla de 27\" curva", leido.getDescripcion());
    }

    @Test
    void csvLeeElFormatoSemillaConColumnasAdicionales() throws Exception {
        Path archivo = carpeta.resolve("semilla.csv");
        Files.writeString(archivo, """
                id,sku,nombre,categoria,subcategoria,marca,precio,descuento_pct,stock,calificacion,num_resenas,imagen,descripcion,fecha_alta,etiquetas
                7,TEC-0007,Audífonos Sonix,Tecnología,Audio,Sonix,320000,15,8,3.7,1512,007.png,Cancelación de ruido.,2026-06-16,"tecnologia,audio"

                """);

        List<Producto> productos = new ProductoCsvDao(archivo).load();

        assertEquals(1, productos.size());
        Producto producto = productos.get(0);
        assertEquals("Audífonos Sonix", producto.getNombre());
        assertEquals("Tecnología", producto.getCategoriaId());
        assertEquals("Sonix", producto.getMarca());
        assertEquals(320000, producto.getPrecio());
        assertEquals(15, producto.getDescuento());
        assertEquals("007.png", producto.getImagen());
        assertTrue(producto.isActivo(), "el archivo semilla no tiene columna 'activo': el producto queda activo");
    }

    @Test
    void csvInexistenteDevuelveListaVacia() throws Exception {
        assertTrue(new ProductoCsvDao(carpeta.resolve("no-existe.csv")).load().isEmpty());
    }

    @Test
    void csvConDatosInvalidosLanzaPersistenceException() throws Exception {
        Path sinCabecera = Files.writeString(carpeta.resolve("sin-cabecera.csv"), "1,SKU,Mouse\n");
        Path filaIncompleta = Files.writeString(carpeta.resolve("incompleta.csv"), "id,nombre,precio\n1,Mouse\n");
        Path precioNoNumerico = Files.writeString(carpeta.resolve("precio.csv"), "id,nombre,precio\n1,Mouse,caro\n");

        assertThrows(PersistenceException.class, () -> new ProductoCsvDao(sinCabecera).load());
        assertThrows(PersistenceException.class, () -> new ProductoCsvDao(filaIncompleta).load());
        assertThrows(PersistenceException.class, () -> new ProductoCsvDao(precioNoNumerico).load());
    }

    @Test
    void guardarCreaLaCarpetaSiNoExiste() throws Exception {
        Path archivo = carpeta.resolve("nueva").resolve("productos.csv");

        new ProductoCsvDao(archivo).save(List.of());

        assertTrue(Files.exists(archivo));
    }

    // ------------------------------------------------------------ usuarios (JSON)

    @Test
    void jsonGuardaYRecuperaUsuarios() throws Exception {
        UsuarioJsonDao dao = new UsuarioJsonDao(carpeta.resolve("usuarios.json"));
        Usuario usuario = new Usuario("u1", "Ana", "ana@test.com", true, RolUsuario.CLIENTE);
        usuario.setPasswordHash("hash");

        dao.save(List.of(usuario));
        Usuario leido = dao.load().get(0);

        assertEquals("u1", leido.getId());
        assertEquals("Ana", leido.getNombre());
        assertEquals("ana@test.com", leido.getCorreo());
        assertEquals(RolUsuario.CLIENTE, leido.getRol());
        assertEquals("hash", leido.getPasswordHash());
    }

    @Test
    void jsonDeUsuariosAceptaElFormatoSemilla() throws Exception {
        Path archivo = Files.writeString(carpeta.resolve("usuarios.json"), """
                [ { "id": "u0", "nombre": "Administrador", "email": "admin@tienda.com", "rol": "ADMIN",
                    "password": "admin123", "tema": "dark", "idioma": "es", "interacciones": [] } ]
                """);

        Usuario usuario = new UsuarioJsonDao(archivo).load().get(0);

        // "email" y "password" se leen como correo y passwordHash; los campos desconocidos se ignoran.
        assertEquals("admin@tienda.com", usuario.getCorreo());
        assertEquals("admin123", usuario.getPasswordHash());
        assertEquals(RolUsuario.ADMIN, usuario.getRol());
        assertTrue(usuario.isEstado(), "sin campo 'estado' el usuario queda activo");
    }

    @Test
    void jsonDeUsuariosInexistenteOVacioDevuelveListaVacia() throws Exception {
        Path vacio = Files.createFile(carpeta.resolve("vacio.json"));

        assertTrue(new UsuarioJsonDao(carpeta.resolve("no-existe.json")).load().isEmpty());
        assertTrue(new UsuarioJsonDao(vacio).load().isEmpty());
    }

    @Test
    void jsonDeUsuariosInvalidoLanzaPersistenceException() throws Exception {
        Path corrupto = Files.writeString(carpeta.resolve("corrupto.json"), "{no-es-json");
        Path noSonUsuarios = Files.writeString(carpeta.resolve("numeros.json"), "[1, 2, 3]");

        assertThrows(PersistenceException.class, () -> new UsuarioJsonDao(corrupto).load());
        assertThrows(PersistenceException.class, () -> new UsuarioJsonDao(noSonUsuarios).load());
    }

    // ------------------------------------------------------------ compras (JSON)

    @Test
    void jsonGuardaYRecuperaCompras() throws Exception {
        CompraJsonDao dao = new CompraJsonDao(carpeta.resolve("compras.json"));
        LocalDateTime fecha = LocalDateTime.of(2026, 9, 30, 10, 15, 0);
        Compra compra = new Compra("c1", "u1", fecha, List.of(new DetalleCompra("p1", 2, 50_000)), 131_000);

        dao.save(List.of(compra));
        Compra leida = dao.load().get(0);

        assertEquals("c1", leida.getId());
        assertEquals("u1", leida.getUsuarioId());
        assertEquals(fecha, leida.getFecha());
        assertEquals(131_000, leida.getTotal());
        assertEquals("p1", leida.getDetalles().get(0).getProductoId());
        assertEquals(2, leida.getDetalles().get(0).getCantidad());
        assertEquals(50_000, leida.getDetalles().get(0).getPrecioUnitario());
    }

    @Test
    void jsonDeComprasAceptaElFormatoSemilla() throws Exception {
        Path archivo = Files.writeString(carpeta.resolve("compras.json"), """
                [ { "id": "P00001", "usuarioId": "u1", "fecha": "2026-07-28", "estado": "ENTREGADO",
                    "items": [ { "productoId": 58, "cantidad": 1, "precioUnitario": 1648150 } ],
                    "total": 1648150 } ]
                """);

        Compra compra = new CompraJsonDao(archivo).load().get(0);

        // "items" se lee como detalles, el id numérico como texto y la fecha sin hora como medianoche.
        assertEquals("58", compra.getDetalles().get(0).getProductoId());
        assertEquals(LocalDateTime.of(2026, 7, 28, 0, 0), compra.getFecha());
        assertEquals(1648150, compra.getTotal());
    }

    @Test
    void jsonDeComprasInexistenteDevuelveListaVaciaEInvalidoLanzaExcepcion() throws Exception {
        Path corrupto = Files.writeString(carpeta.resolve("corrupto.json"), "[{\"id\": ");
        Path noSonCompras = Files.writeString(carpeta.resolve("textos.json"), "[\"a\"]");

        assertTrue(new CompraJsonDao(carpeta.resolve("no-existe.json")).load().isEmpty());
        assertThrows(PersistenceException.class, () -> new CompraJsonDao(corrupto).load());
        assertThrows(PersistenceException.class, () -> new CompraJsonDao(noSonCompras).load());
    }
}
