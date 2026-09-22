package uptc.repository;

import uptc.model.Producto;
import uptc.exception.EntityNotFoundException;
import uptc.exception.PersistenceException;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
    
    Producto save(Producto producto) throws PersistenceException;
    
    Optional<Producto> findById(String id) throws PersistenceException;

    List<Producto> findAll() throws PersistenceException;

    void deleteById(String id) throws EntityNotFoundException, PersistenceException;

    boolean existsById(String id) throws PersistenceException;
}