package uptc.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import uptc.exception.EntityNotFoundException;
import uptc.exception.ValidationException;
import uptc.model.Categoria;
import uptc.model.Usuario;
import uptc.utils.Validaciones;

/** CRUD de categorías y de su relación jerárquica padre-hijo. */
public class CategoriaController {
    private final List<Categoria> categories = new ArrayList<>();

    /** Crea el controlador con las categorías iniciales (las que ya existen en el catálogo). */
    public CategoriaController(List<Categoria> initialCategories) {
        categories.addAll(initialCategories);
    }

    /** Crea una categoría raíz o hija de otra existente. Solo ADMIN. */
    public Categoria create(Usuario admin, Categoria category) throws ValidationException {
        Validaciones.admin(admin);
        validate(category);
        if (find(category.getId()) != null) {
            throw new ValidationException("La categoría ya existe");
        }
        categories.add(category);
        return category;
    }

    /** Reemplaza los datos de una categoría existente. Solo ADMIN. */
    public Categoria update(Usuario admin, Categoria category) throws ValidationException, EntityNotFoundException {
        Validaciones.admin(admin);
        validate(category);
        Categoria current = find(category.getId());
        if (current == null) {
            throw new EntityNotFoundException("Categoría no encontrada: " + category.getId());
        }
        categories.set(categories.indexOf(current), category);
        return category;
    }

    /** Elimina una categoría que no tenga subcategorías. Solo ADMIN. */
    public void delete(Usuario admin, String id) throws ValidationException, EntityNotFoundException {
        Validaciones.admin(admin);
        Categoria category = find(id);
        if (category == null) {
            throw new EntityNotFoundException("Categoría no encontrada: " + id);
        }
        if (!childrenOf(id).isEmpty()) {
            throw new ValidationException("No se puede eliminar una categoría con subcategorías");
        }
        categories.remove(category);
    }

    /** Busca una categoría por id. Devuelve null si no existe. */
    public Categoria find(String id) {
        for (Categoria category : categories) {
            if (Objects.equals(category.getId(), id)) {
                return category;
            }
        }
        return null;
    }

    /** Devuelve una copia de la lista de categorías. */
    public List<Categoria> findAll() {
        return List.copyOf(categories);
    }

    /** Devuelve las subcategorías directas de una categoría. */
    public List<Categoria> childrenOf(String parentId) {
        List<Categoria> children = new ArrayList<>();
        for (Categoria category : categories) {
            if (Objects.equals(parentId, category.getCategoriaPadreId())) {
                children.add(category);
            }
        }
        return children;
    }

    private void validate(Categoria category) throws ValidationException {
        if (category == null || Validaciones.vacio(category.getId()) || Validaciones.vacio(category.getNombre())) {
            throw new ValidationException("ID y nombre de categoría son obligatorios");
        }
        String parentId = category.getCategoriaPadreId();
        if (parentId != null && (parentId.equals(category.getId()) || find(parentId) == null)) {
            throw new ValidationException("La categoría padre no existe");
        }
    }
}
