package uptc.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import uptc.exception.EntityNotFoundException;
import uptc.exception.PersistenceException;
import uptc.model.Categoria;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonCategoriaRepository implements CategoriaRepository {

    private final File jsonFile;
    private final ObjectMapper objectMapper;

    public JsonCategoriaRepository(String filePath) {
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
                objectMapper.writeValue(jsonFile, new ArrayList<Categoria>());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al inicializar el archivo de almacenamiento JSON: " + jsonFile.getPath(), e);
        }
    }

    @Override
    public synchronized List<Categoria> findAll() throws PersistenceException {
        if (!jsonFile.exists() || jsonFile.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(jsonFile, new TypeReference<List<Categoria>>() {});
        } catch (IOException e) {
            throw new PersistenceException("Error al leer las categorías desde el JSON: " + e.getMessage());
        }
    }

    @Override
    public synchronized Optional<Categoria> findById(String id) throws PersistenceException {
        return findAll().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    @Override
    public synchronized Categoria save(Categoria categoria) throws PersistenceException {
        List<Categoria> categorias = findAll();

        boolean actualizado = false;
        for (int i = 0; i < categorias.size(); i++) {
            if (categorias.get(i).getId().equals(categoria.getId())) {
                categorias.set(i, categoria);
                actualizado = true;
                break;
            }
        }

        if (!actualizado) {
            categorias.add(categoria);
        }

        guardarTodas(categorias);
        return categoria;
    }

    @Override
    public synchronized void deleteById(String id) throws EntityNotFoundException, PersistenceException {
        List<Categoria> categorias = findAll();
        boolean eliminado = categorias.removeIf(c -> c.getId().equals(id));

        if (!eliminado) {
            throw new EntityNotFoundException("No se encontró la categoría con ID: " + id + " para eliminar.");
        }

        guardarTodas(categorias);
    }

    @Override
    public synchronized boolean existsById(String id) throws PersistenceException {
        return findById(id).isPresent();
    }

    private void guardarTodas(List<Categoria> categorias) throws PersistenceException {
        try {
            objectMapper.writeValue(jsonFile, categorias);
        } catch (IOException e) {
            throw new PersistenceException("Error al escribir las categorías en el JSON: " + e.getMessage());
        }
    }
}