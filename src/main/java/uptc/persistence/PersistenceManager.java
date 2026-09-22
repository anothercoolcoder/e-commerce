package uptc.persistence;

import uptc.repository.CategoriaRepository;
import uptc.repository.JsonCategoriaRepository;
import uptc.repository.JsonProductoRepository;
import uptc.repository.JsonUsuarioRepository;
import uptc.repository.ProductoRepository;
import uptc.repository.UsuarioRepository;

import java.io.File;

public class PersistenceManager {

    private static PersistenceManager instance;

    private final String dataDirectory;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    private PersistenceManager(String dataDirectory) {
        this.dataDirectory = dataDirectory;
        asegurarDirectorioExiste(dataDirectory);

        String productosPath = dataDirectory + File.separator + "productos.json";
        String categoriasPath = dataDirectory + File.separator + "categorias.json";
        String usuariosPath = dataDirectory + File.separator + "usuarios.json";

        this.productoRepository = new JsonProductoRepository(productosPath);
        this.categoriaRepository = new JsonCategoriaRepository(categoriasPath);
        this.usuarioRepository = new JsonUsuarioRepository(usuariosPath);
    }

    public static synchronized PersistenceManager getInstance() {
        if (instance == null) {
            String defaultPath = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "resources-data";
            instance = new PersistenceManager(defaultPath);
        }
        return instance;
    }

    public static synchronized PersistenceManager getInstance(String customDataDirectory) {
        if (instance == null) {
            instance = new PersistenceManager(customDataDirectory);
        }
        return instance;
    }

    private void asegurarDirectorioExiste(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public ProductoRepository getProductoRepository() {
        return productoRepository;
    }

    public CategoriaRepository getCategoriaRepository() {
        return categoriaRepository;
    }

    public UsuarioRepository getUsuarioRepository() {
        return usuarioRepository;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }
}