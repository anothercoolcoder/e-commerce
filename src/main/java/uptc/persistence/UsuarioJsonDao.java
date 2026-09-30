package uptc.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import uptc.exception.PersistenceException;
import uptc.model.Usuario;

/** DAO JSON de usuarios. */
public class UsuarioJsonDao {
    private final Path file;
    private final ObjectMapper mapper = PersistenceManager.jsonMapper();

    public UsuarioJsonDao(Path file) {
        this.file = file;
    }

    /** Lee todos los usuarios, o devuelve una lista vacía si el archivo aún no existe. */
    public List<Usuario> load() throws PersistenceException {
        try {
            if (!Files.exists(file) || Files.size(file) == 0) {
                return new ArrayList<>();
            }
            List<Usuario> users = new ArrayList<>();
            for (JsonNode node : mapper.readTree(file.toFile())) {
                if (!node.isObject()) {
                    throw new PersistenceException("JSON inválido: " + file + " — se esperaba un usuario");
                }
                ObjectNode user = (ObjectNode) node;
                // El archivo semilla usa "email" y "password"; el modelo usa "correo" y "passwordHash".
                rename(user, "email", "correo");
                rename(user, "password", "passwordHash");
                users.add(mapper.treeToValue(user, Usuario.class));
            }
            return users;
        } catch (IOException e) {
            throw new PersistenceException("JSON inválido: " + file + " — " + e.getMessage(), e);
        }
    }

    /** Reemplaza el archivo por la lista recibida. */
    public void save(List<Usuario> users) throws PersistenceException {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            mapper.writeValue(file.toFile(), users);
        } catch (IOException e) {
            throw new PersistenceException("No se pudo guardar " + file + ": " + e.getMessage(), e);
        }
    }

    private void rename(ObjectNode node, String oldName, String newName) {
        if (node.has(oldName) && !node.has(newName)) {
            node.set(newName, node.remove(oldName));
        }
    }
}
