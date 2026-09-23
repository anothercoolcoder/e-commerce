package uptc.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import uptc.exception.EntityNotFoundException;
import uptc.exception.PersistenceException;
import uptc.model.Usuario;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonUsuarioRepository implements UsuarioRepository {

    private final File jsonFile;
    private final ObjectMapper objectMapper;

    public JsonUsuarioRepository(String filePath) {
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
                objectMapper.writeValue(jsonFile, new ArrayList<Usuario>());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al inicializar el archivo de almacenamiento JSON: " + jsonFile.getPath(), e);
        }
    }

    @Override
    public synchronized List<Usuario> findAll() throws PersistenceException {
        if (!jsonFile.exists() || jsonFile.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(jsonFile, new TypeReference<List<Usuario>>() {});
        } catch (IOException e) {
            throw new PersistenceException("Error al leer los usuarios desde el JSON: " + e.getMessage());
        }
    }

    @Override
    public synchronized Optional<Usuario> findById(String id) throws PersistenceException {
        return findAll().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    @Override
    public synchronized Optional<Usuario> findByCorreo(String correo) throws PersistenceException {
        return findAll().stream()
                .filter(u -> u.getCorreo() != null && u.getCorreo().equalsIgnoreCase(correo))
                .findFirst();
    }

    @Override
    public synchronized Usuario save(Usuario usuario) throws PersistenceException {
        List<Usuario> usuarios = findAll();

        boolean actualizado = false;
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getId().equals(usuario.getId())) {
                usuarios.set(i, usuario);
                actualizado = true;
                break;
            }
        }

        if (!actualizado) {
            usuarios.add(usuario);
        }

        guardarTodos(usuarios);
        return usuario;
    }

    @Override
    public synchronized void deleteById(String id) throws EntityNotFoundException, PersistenceException {
        List<Usuario> usuarios = findAll();
        boolean eliminado = usuarios.removeIf(u -> u.getId().equals(id));

        if (!eliminado) {
            throw new EntityNotFoundException("No se encontró el usuario con ID: " + id + " para eliminar.");
        }

        guardarTodos(usuarios);
    }

    @Override
    public synchronized boolean existsById(String id) throws PersistenceException {
        return findById(id).isPresent();
    }

    private void guardarTodos(List<Usuario> usuarios) throws PersistenceException {
        try {
            objectMapper.writeValue(jsonFile, usuarios);
        } catch (IOException e) {
            throw new PersistenceException("Error al escribir los usuarios en el JSON: " + e.getMessage());
        }
    }
}