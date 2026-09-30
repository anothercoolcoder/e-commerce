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
import uptc.model.Compra;

/** DAO JSON de los pedidos confirmados. */
public class CompraJsonDao {
    private final Path file;
    private final ObjectMapper mapper = PersistenceManager.jsonMapper();

    public CompraJsonDao(Path file) {
        this.file = file;
    }

    /** Lee todos los pedidos, o devuelve una lista vacía si el archivo aún no existe. */
    public List<Compra> load() throws PersistenceException {
        try {
            if (!Files.exists(file) || Files.size(file) == 0) {
                return new ArrayList<>();
            }
            List<Compra> purchases = new ArrayList<>();
            for (JsonNode node : mapper.readTree(file.toFile())) {
                if (!node.isObject()) {
                    throw new PersistenceException("JSON inválido: " + file + " — se esperaba un pedido");
                }
                ObjectNode purchase = (ObjectNode) node;
                // El archivo semilla llama "items" a los detalles y guarda la fecha sin hora.
                if (purchase.has("items") && !purchase.has("detalles")) {
                    purchase.set("detalles", purchase.remove("items"));
                }
                if (purchase.path("fecha").isTextual() && purchase.get("fecha").asText().length() == 10) {
                    purchase.put("fecha", purchase.get("fecha").asText() + "T00:00:00");
                }
                purchases.add(mapper.treeToValue(purchase, Compra.class));
            }
            return purchases;
        } catch (IOException e) {
            throw new PersistenceException("JSON inválido: " + file + " — " + e.getMessage(), e);
        }
    }

    /** Reemplaza el archivo por la lista completa de pedidos. */
    public void save(List<Compra> purchases) throws PersistenceException {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            mapper.writeValue(file.toFile(), purchases);
        } catch (IOException e) {
            throw new PersistenceException("No se pudo guardar " + file + ": " + e.getMessage(), e);
        }
    }
}
