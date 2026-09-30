package uptc.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uptc.model.Compra;
import uptc.model.Producto;
import uptc.model.Usuario;

/**
 * Prueba de integración con los datos semilla empaquetados en
 * {@code src/main/resources/resources-data}. Trabaja sobre una copia en una
 * carpeta temporal, así no depende de lo que la aplicación haya escrito en
 * la carpeta real {@code data}.
 */
class SeedCatalogIntegrationTest {
    @TempDir
    Path carpeta;

    @BeforeEach
    void usarCarpetaTemporal() {
        System.setProperty(PersistenceManager.DATA_PROPERTY, carpeta.toString());
        PersistenceManager.reset();
    }

    @AfterEach
    void restaurarConfiguracion() {
        System.clearProperty(PersistenceManager.DATA_PROPERTY);
        PersistenceManager.reset();
    }

    @Test
    void enUnaInstalacionNuevaSeCopianYSeLeenLosDatosSemilla() throws Exception {
        PersistenceManager gestor = PersistenceManager.getInstance();

        gestor.copyMissingSeedFiles();
        List<Producto> productos = gestor.productos().load();
        List<Usuario> usuarios = gestor.usuarios().load();
        List<Compra> compras = gestor.compras().load();

        assertEquals(240, productos.size());
        assertEquals(5, usuarios.size());
        assertEquals(49, compras.size());
        assertEquals("001.png", productos.get(0).getImagen());
        assertFalse(productos.get(0).getNombre().isBlank());
        assertEquals("admin@tienda.com", usuarios.get(0).getCorreo());
        assertEquals(2, compras.get(0).getDetalles().size());
    }

    @Test
    void losArchivosQueYaExistenNoSeSobrescriben() throws Exception {
        Files.writeString(carpeta.resolve("usuarios.json"), "[]");
        PersistenceManager gestor = PersistenceManager.getInstance();

        gestor.copyMissingSeedFiles();

        assertEquals(0, gestor.usuarios().load().size());
        assertEquals(240, gestor.productos().load().size());
    }
}
