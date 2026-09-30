package uptc.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uptc.model.RolUsuario;
import uptc.model.Usuario;

class PersistenceManagerTest {
    @TempDir
    Path carpeta;

    /** Cada prueba usa su propia carpeta temporal en lugar de la carpeta real "data". */
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
    void esUnSingleton() {
        PersistenceManager primera = PersistenceManager.getInstance();
        PersistenceManager segunda = PersistenceManager.getInstance();

        assertSame(primera, segunda);
        assertSame(primera.productos(), segunda.productos());
    }

    @Test
    void resetDescartaLaInstanciaParaCrearOtra() {
        PersistenceManager primera = PersistenceManager.getInstance();

        PersistenceManager.reset();

        assertNotSame(primera, PersistenceManager.getInstance());
    }

    @Test
    void usaLaCarpetaDataSiNoSeConfiguraOtra() {
        System.clearProperty(PersistenceManager.DATA_PROPERTY);
        PersistenceManager.reset();

        assertEquals(Path.of("data"), PersistenceManager.getInstance().getBaseDirectory());
    }

    @Test
    void losDaoLeenYEscribenDentroDeLaCarpetaConfigurada() throws Exception {
        PersistenceManager gestor = PersistenceManager.getInstance();

        gestor.usuarios().save(List.of(new Usuario("u1", "Ana", "ana@test.com", true, RolUsuario.CLIENTE)));

        assertEquals(carpeta, gestor.getBaseDirectory());
        assertTrue(Files.exists(carpeta.resolve("usuarios.json")));
        assertTrue(gestor.productos().load().isEmpty());
        assertTrue(gestor.compras().load().isEmpty());
    }
}
