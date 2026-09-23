package uptc.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import uptc.exception.EntityNotFoundException;
import uptc.exception.PersistenceException;
import uptc.model.Producto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonProductoRepository implements ProductoRepository {

    private final File jsonFile;
    private final ObjectMapper objectMapper;

    public JsonProductoRepository(String filePath) {
        this.jsonFile = new File(filePath);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        asegurarArchivoExiste();
    }

    private synchronized void asegurarArchivoExiste() {
        try {
            if (!jsonFile.exists()) {
                File parent = jsonFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                jsonFile.createNewFile();
                objectMapper.writeValue(jsonFile, new ArrayList<Producto>());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al inicializar el archivo de almacenamiento JSON: " + jsonFile.getPath(), e);
        }
    }

    @Override
    public synchronized List<Producto> findAll() throws PersistenceException {
        if (!jsonFile.exists() || jsonFile.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(jsonFile, new TypeReference<List<Producto>>() {});
        } catch (IOException e) {
            throw new PersistenceException("Error al leer los productos desde el JSON: " + e.getMessage());
        }
    }

    @Override
    public synchronized Optional<Producto> findById(String id) throws PersistenceException {
        return findAll().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    @Override
    public synchronized Producto save(Producto producto) throws PersistenceException {
        List<Producto> productos = findAll();
        
        boolean actualizado = false;
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId().equals(producto.getId())) {
                productos.set(i, producto);
                actualizado = true;
                break;
            }
        }
        
        if (!actualizado) {
            productos.add(producto);
        }

        guardarTodos(productos);
        return producto;
    }

    @Override
    public synchronized void deleteById(String id) throws EntityNotFoundException, PersistenceException {
        List<Producto> productos = findAll();
        boolean eliminado = productos.removeIf(p -> p.getId().equals(id));

        if (!eliminado) {
            throw new EntityNotFoundException("No se encontró el producto con ID: " + id + " para eliminar.");
        }

        guardarTodos(productos);
    }

    @Override
    public synchronized boolean existsById(String id) throws PersistenceException {
        return findById(id).isPresent();
    }

    private void guardarTodos(List<Producto> productos) throws PersistenceException {
        try {
            objectMapper.writeValue(jsonFile, productos);
        } catch (IOException e) {
            throw new PersistenceException("Error al escribir los productos en el JSON: " + e.getMessage());
        }
    }
}