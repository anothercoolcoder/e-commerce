package uptc.repository;

import uptc.model.Usuario;
import uptc.exception.EntityNotFoundException;
import uptc.exception.PersistenceException;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    Usuario save(Usuario usuario) throws PersistenceException;

    Optional<Usuario> findById(String id) throws PersistenceException;

    Optional<Usuario> findByCorreo(String correo) throws PersistenceException;

    List<Usuario> findAll() throws PersistenceException;

    void deleteById(String id) throws EntityNotFoundException, PersistenceException;

    boolean existsById(String id) throws PersistenceException;
}