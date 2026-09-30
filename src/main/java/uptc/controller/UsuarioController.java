package uptc.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import uptc.exception.AutenticacionException;
import uptc.exception.PersistenceException;
import uptc.exception.ValidationException;
import uptc.model.RolUsuario;
import uptc.model.Usuario;
import uptc.persistence.UsuarioJsonDao;
import uptc.utils.PasswordHasher;
import uptc.utils.Validaciones;

/** Registro, autenticación y consulta de usuarios. */
public class UsuarioController {
    private final UsuarioJsonDao dao;
    private final List<Usuario> users = new ArrayList<>();

    /** Carga los usuarios desde el JSON. */
    public UsuarioController(UsuarioJsonDao dao) throws PersistenceException {
        this.dao = Objects.requireNonNull(dao);
        users.addAll(dao.load());
    }

    /** Registra un usuario activo guardando únicamente el hash de su contraseña. */
    public Usuario register(String id, String name, String email, String password, RolUsuario role)
            throws ValidationException, PersistenceException {
        if (Validaciones.vacio(id) || Validaciones.vacio(name) || Validaciones.vacio(password) || role == null) {
            throw new ValidationException("Datos de usuario incompletos");
        }
        Validaciones.correo(email);
        for (Usuario existing : users) {
            if (id.equals(existing.getId()) || email.equalsIgnoreCase(existing.getCorreo())) {
                throw new ValidationException("El usuario o correo ya existe");
            }
        }
        Usuario user = new Usuario(id, name, email, true, role);
        user.setPasswordHash(PasswordHasher.hash(password));
        users.add(user);
        dao.save(users);
        return user;
    }

    /**
     * Autentica por correo y contraseña. El mensaje de error es el mismo si el
     * correo no existe, si el usuario está inactivo o si la contraseña no
     * coincide, para no revelar cuál de los datos falló.
     */
    public Usuario login(String email, String password) {
        for (Usuario user : users) {
            boolean sameEmail = user.getCorreo() != null && user.getCorreo().equalsIgnoreCase(email);
            if (sameEmail && user.isEstado() && PasswordHasher.matches(password, user.getPasswordHash())) {
                return user;
            }
        }
        throw new AutenticacionException("Credenciales inválidas");
    }

    /** Devuelve una copia de la lista de usuarios. */
    public List<Usuario> findAll() {
        return List.copyOf(users);
    }
}
