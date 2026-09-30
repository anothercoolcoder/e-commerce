package uptc.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uptc.DatosDePrueba.ADMIN;
import static uptc.DatosDePrueba.CLIENTE;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uptc.exception.EntityNotFoundException;
import uptc.exception.ValidationException;
import uptc.model.Categoria;

class CategoriaControllerTest {
    private CategoriaController categorias;

    @BeforeEach
    void crearCategorias() throws Exception {
        categorias = new CategoriaController(List.of(new Categoria("tec", "Tecnología", "")));
        categorias.create(ADMIN, hija("audio", "Audio", "tec"));
    }

    private Categoria hija(String id, String nombre, String padreId) {
        Categoria categoria = new Categoria(id, nombre, "");
        categoria.setCategoriaPadreId(padreId);
        return categoria;
    }

    @Test
    void creaCategoriasRaizYSubcategorias() throws Exception {
        categorias.create(ADMIN, new Categoria("hogar", "Hogar", "Cosas de casa"));

        assertEquals(3, categorias.findAll().size());
        assertEquals("Audio", categorias.childrenOf("tec").get(0).getNombre());
        assertEquals("Hogar", categorias.find("hogar").getNombre());
    }

    @Test
    void rechazaCategoriasInvalidasORepetidas() {
        assertThrows(ValidationException.class, () -> categorias.create(ADMIN, null));
        assertThrows(ValidationException.class, () -> categorias.create(ADMIN, new Categoria("", "Sin id", "")));
        assertThrows(ValidationException.class, () -> categorias.create(ADMIN, new Categoria("x", " ", "")));
        assertThrows(ValidationException.class, () -> categorias.create(ADMIN, new Categoria("tec", "Repetida", "")));
    }

    @Test
    void laCategoriaPadreDebeExistir() {
        assertThrows(ValidationException.class, () -> categorias.create(ADMIN, hija("x", "Huérfana", "no-existe")));
        assertThrows(ValidationException.class, () -> categorias.create(ADMIN, hija("x", "Padre de sí misma", "x")));
    }

    @Test
    void actualizaUnaCategoriaExistente() throws Exception {
        categorias.update(ADMIN, hija("audio", "Audio y sonido", "tec"));

        assertEquals("Audio y sonido", categorias.find("audio").getNombre());
        assertEquals(2, categorias.findAll().size());
    }

    @Test
    void actualizarOEliminarUnaCategoriaInexistenteLanzaExcepcion() {
        assertThrows(EntityNotFoundException.class, () -> categorias.update(ADMIN, new Categoria("x", "Nada", "")));
        assertThrows(EntityNotFoundException.class, () -> categorias.delete(ADMIN, "x"));
    }

    @Test
    void noSePuedeEliminarUnaCategoriaConSubcategorias() {
        assertThrows(ValidationException.class, () -> categorias.delete(ADMIN, "tec"));
        assertEquals(2, categorias.findAll().size());
    }

    @Test
    void eliminaUnaCategoriaSinSubcategorias() throws Exception {
        categorias.delete(ADMIN, "audio");
        categorias.delete(ADMIN, "tec");

        assertNull(categorias.find("audio"));
        assertEquals(0, categorias.findAll().size());
    }

    @Test
    void soloUnAdminPuedeModificarLasCategorias() {
        assertThrows(ValidationException.class, () -> categorias.create(CLIENTE, new Categoria("x", "Nueva", "")));
        assertThrows(ValidationException.class, () -> categorias.update(CLIENTE, new Categoria("tec", "Otra", "")));
        assertThrows(ValidationException.class, () -> categorias.delete(CLIENTE, "audio"));
        assertEquals(2, categorias.findAll().size());
    }
}
