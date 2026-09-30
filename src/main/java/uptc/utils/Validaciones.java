package uptc.utils;

import java.util.regex.Pattern;
import uptc.exception.ValidationException;
import uptc.model.Producto;
import uptc.model.RolUsuario;
import uptc.model.Usuario;

/** Reglas reutilizables de validación del dominio. */
public final class Validaciones {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validaciones() {
    }

    /** Valida todos los campos comerciales obligatorios de un producto. */
    public static void producto(Producto producto) throws ValidationException {
        if (producto == null) {
            throw new ValidationException("El producto es obligatorio");
        }
        if (vacio(producto.getId()) || vacio(producto.getSku())) {
            throw new ValidationException("El ID y el SKU son obligatorios");
        }
        if (vacio(producto.getNombre())) {
            throw new ValidationException("El nombre del producto es obligatorio");
        }
        if (producto.getPrecio() <= 0) {
            throw new ValidationException("El precio debe ser mayor que cero");
        }
        if (producto.getStock() < 0) {
            throw new ValidationException("El stock no puede ser negativo");
        }
        if (producto.getDescuento() < 0 || producto.getDescuento() > 100) {
            throw new ValidationException("El descuento debe estar entre 0 y 100");
        }
        if (producto.getCalificacion() < 0 || producto.getCalificacion() > 5) {
            throw new ValidationException("La calificación debe estar entre 0 y 5");
        }
    }

    /** Valida una dirección de correo básica. */
    public static void correo(String correo) throws ValidationException {
        if (vacio(correo) || !EMAIL.matcher(correo).matches()) {
            throw new ValidationException("Correo inválido");
        }
    }

    /** Exige que quien realiza la operación tenga el rol ADMIN. */
    public static void admin(Usuario usuario) throws ValidationException {
        if (usuario == null || usuario.getRol() != RolUsuario.ADMIN) {
            throw new ValidationException("Solo un usuario ADMIN puede realizar esta operación");
        }
    }

    /** Devuelve true cuando el texto es nulo o solo contiene espacios. */
    public static boolean vacio(String text) {
        return text == null || text.isBlank();
    }
}
