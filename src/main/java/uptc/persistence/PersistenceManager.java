package uptc.persistence;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import uptc.exception.PersistenceException;

/**
 * Gestor de persistencia (patrón Singleton): única puerta de acceso a la
 * carpeta de datos y a los DAO de la aplicación.
 *
 * <p>La carpeta es {@code data} por defecto. Se puede cambiar con la propiedad
 * de sistema {@code shoptree.data}, que es lo que hacen las pruebas para no
 * tocar los datos reales.</p>
 */
public final class PersistenceManager {
    /** Propiedad de sistema que indica la carpeta de datos. */
    public static final String DATA_PROPERTY = "shoptree.data";
    private static final String[] SEED_FILES = {"productos.csv", "usuarios.json", "compras.json"};

    private static PersistenceManager instance;

    private final Path baseDirectory;
    private final ProductoCsvDao productoDao;
    private final UsuarioJsonDao usuarioDao;
    private final CompraJsonDao compraDao;

    private PersistenceManager(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
        productoDao = new ProductoCsvDao(baseDirectory.resolve("productos.csv"));
        usuarioDao = new UsuarioJsonDao(baseDirectory.resolve("usuarios.json"));
        compraDao = new CompraJsonDao(baseDirectory.resolve("compras.json"));
    }

    /** Devuelve la única instancia; la crea la primera vez que se pide. */
    public static synchronized PersistenceManager getInstance() {
        if (instance == null) {
            instance = new PersistenceManager(Path.of(System.getProperty(DATA_PROPERTY, "data")));
        }
        return instance;
    }

    /** Descarta la instancia para que la siguiente llamada la cree de nuevo (uso en pruebas). */
    public static synchronized void reset() {
        instance = null;
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }

    /**
     * En una instalación nueva copia a la carpeta de datos los archivos semilla
     * empaquetados en {@code resources/resources-data}. Los archivos que ya
     * existen no se tocan.
     */
    public void copyMissingSeedFiles() throws PersistenceException {
        try {
            Files.createDirectories(baseDirectory);
            for (String name : SEED_FILES) {
                Path target = baseDirectory.resolve(name);
                if (Files.exists(target)) {
                    continue;
                }
                try (InputStream source = PersistenceManager.class.getResourceAsStream("/resources-data/" + name)) {
                    if (source != null) {
                        Files.copy(source, target);
                    }
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("No se pudieron preparar los datos iniciales: " + e.getMessage(), e);
        }
    }

    public ProductoCsvDao productos() {
        return productoDao;
    }

    public UsuarioJsonDao usuarios() {
        return usuarioDao;
    }

    public CompraJsonDao compras() {
        return compraDao;
    }

    /** Configuración de Jackson compartida por los DAO JSON. */
    static ObjectMapper jsonMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }
}
