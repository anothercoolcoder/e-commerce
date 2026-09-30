package uptc.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uptc.DatosDePrueba.ADMIN;
import static uptc.DatosDePrueba.CLIENTE;

import org.junit.jupiter.api.Test;
import uptc.DatosDePrueba;
import uptc.exception.ValidationException;
import uptc.model.Producto;

class ValidacionesTest {

    private Producto productoValido() {
        return DatosDePrueba.producto("1", "Mouse", "Tecnología", "Nova", 80_000, 5);
    }

    @Test
    void aceptaUnProductoValido() {
        assertDoesNotThrow(() -> Validaciones.producto(productoValido()));
    }

    @Test
    void rechazaProductoNuloOSinIdentificadores() {
        Producto sinId = productoValido();
        sinId.setId(" ");
        Producto sinSku = productoValido();
        sinSku.setSku(null);
        Producto sinNombre = productoValido();
        sinNombre.setNombre("");

        assertThrows(ValidationException.class, () -> Validaciones.producto(null));
        assertThrows(ValidationException.class, () -> Validaciones.producto(sinId));
        assertThrows(ValidationException.class, () -> Validaciones.producto(sinSku));
        assertThrows(ValidationException.class, () -> Validaciones.producto(sinNombre));
    }

    @Test
    void rechazaPrecioStockDescuentoOCalificacionFueraDeRango() {
        Producto precioCero = productoValido();
        precioCero.setPrecio(0);
        Producto stockNegativo = productoValido();
        stockNegativo.setStock(-1);
        Producto descuentoNegativo = productoValido();
        descuentoNegativo.setDescuento(-5);
        Producto descuentoExcesivo = productoValido();
        descuentoExcesivo.setDescuento(101);
        Producto calificacionNegativa = productoValido();
        calificacionNegativa.setCalificacion(-1);
        Producto calificacionExcesiva = productoValido();
        calificacionExcesiva.setCalificacion(5.1);

        assertThrows(ValidationException.class, () -> Validaciones.producto(precioCero));
        assertThrows(ValidationException.class, () -> Validaciones.producto(stockNegativo));
        assertThrows(ValidationException.class, () -> Validaciones.producto(descuentoNegativo));
        assertThrows(ValidationException.class, () -> Validaciones.producto(descuentoExcesivo));
        assertThrows(ValidationException.class, () -> Validaciones.producto(calificacionNegativa));
        assertThrows(ValidationException.class, () -> Validaciones.producto(calificacionExcesiva));
    }

    @Test
    void validaElFormatoDelCorreo() {
        assertDoesNotThrow(() -> Validaciones.correo("ana@correo.com"));
        assertThrows(ValidationException.class, () -> Validaciones.correo("sin-arroba.com"));
        assertThrows(ValidationException.class, () -> Validaciones.correo("ana@sin-punto"));
        assertThrows(ValidationException.class, () -> Validaciones.correo("con espacio@correo.com"));
        assertThrows(ValidationException.class, () -> Validaciones.correo(""));
        assertThrows(ValidationException.class, () -> Validaciones.correo(null));
    }

    @Test
    void soloElRolAdminPasaLaValidacionDeAdministrador() {
        assertDoesNotThrow(() -> Validaciones.admin(ADMIN));
        assertThrows(ValidationException.class, () -> Validaciones.admin(CLIENTE));
        assertThrows(ValidationException.class, () -> Validaciones.admin(null));
    }

    @Test
    void vacioDetectaNulosYEspacios() {
        assertTrue(Validaciones.vacio(null));
        assertTrue(Validaciones.vacio("   "));
        assertFalse(Validaciones.vacio("texto"));
    }
}
