package uptc.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uptc.exception.AutenticacionException;
import uptc.exception.ValidationException;
import uptc.model.RolUsuario;
import uptc.model.Usuario;
import uptc.persistence.UsuarioJsonDao;

class UsuarioControllerTest {
    @TempDir
    Path carpeta;

    private UsuarioJsonDao dao;
    private UsuarioController usuarios;

    @BeforeEach
    void crearControlador() throws Exception {
        dao = new UsuarioJsonDao(carpeta.resolve("usuarios.json"));
        usuarios = new UsuarioController(dao);
        usuarios.register("ana", "Ana", "ana@test.com", "secreto", RolUsuario.CLIENTE);
    }

    // ------------------------------------------------------------ registro

    @Test
    void registraUnUsuarioActivoConSuRol() throws Exception {
        Usuario admin = usuarios.register("root", "Root", "root@test.com", "clave", RolUsuario.ADMIN);

        assertEquals(RolUsuario.ADMIN, admin.getRol());
        assertTrue(admin.isEstado());
        assertEquals(2, usuarios.findAll().size());
    }

    @Test
    void guardaElHashDeLaContrasenaYNoElTextoOriginal() throws Exception {
        List<Usuario> guardados = dao.load();

        assertEquals(1, guardados.size());
        assertNotEquals("secreto", guardados.get(0).getPasswordHash());
        assertEquals(64, guardados.get(0).getPasswordHash().length()); // SHA-256 en hexadecimal
    }

    @Test
    void rechazaIdOCorreoRepetidos() {
        assertThrows(ValidationException.class,
                () -> usuarios.register("ana", "Otra", "otra@test.com", "clave", RolUsuario.CLIENTE));
        assertThrows(ValidationException.class,
                () -> usuarios.register("ana2", "Otra", "ANA@test.com", "clave", RolUsuario.CLIENTE));
    }

    @Test
    void rechazaDatosIncompletosOCorreoInvalido() {
        assertThrows(ValidationException.class,
                () -> usuarios.register("", "Luis", "luis@test.com", "clave", RolUsuario.CLIENTE));
        assertThrows(ValidationException.class,
                () -> usuarios.register("luis", "Luis", "luis@test.com", "", RolUsuario.CLIENTE));
        assertThrows(ValidationException.class,
                () -> usuarios.register("luis", "Luis", "luis@test.com", "clave", null));
        assertThrows(ValidationException.class,
                () -> usuarios.register("luis", "Luis", "correo-sin-arroba", "clave", RolUsuario.CLIENTE));
        assertEquals(1, usuarios.findAll().size());
    }

    // ------------------------------------------------------------ autenticación

    @Test
    void iniciaSesionConCorreoYContrasenaCorrectos() {
        assertEquals("ana", usuarios.login("ana@test.com", "secreto").getId());
        assertEquals("ana", usuarios.login("ANA@TEST.COM", "secreto").getId());
    }

    @Test
    void rechazaContrasenaIncorrectaOCorreoDesconocido() {
        assertThrows(AutenticacionException.class, () -> usuarios.login("ana@test.com", "incorrecta"));
        assertThrows(AutenticacionException.class, () -> usuarios.login("ana@test.com", ""));
        assertThrows(AutenticacionException.class, () -> usuarios.login("nadie@test.com", "secreto"));
    }

    @Test
    void unUsuarioInactivoNoPuedeIniciarSesion() {
        usuarios.findAll().get(0).setEstado(false);

        assertThrows(AutenticacionException.class, () -> usuarios.login("ana@test.com", "secreto"));
    }

    @Test
    void losUsuariosRegistradosSeCarganAlCrearOtroControlador() throws Exception {
        UsuarioController otraSesion = new UsuarioController(dao);

        assertEquals("ana", otraSesion.login("ana@test.com", "secreto").getId());
    }
}
