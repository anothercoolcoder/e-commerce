package uptc.repository;

import uptc.model.Categoria;
import uptc.exception.EntityNotFoundException;
import uptc.exception.PersistenceException;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository {

    Categoria save(Categoria categoria) throws PersistenceException;

    Optional<Categoria> findById(String id) throws PersistenceException;

    List<Categoria> findAll() throws PersistenceException;

    void deleteById(String id) throws EntityNotFoundException, PersistenceException;

    boolean existsById(String id) throws PersistenceException;
}