package uptc.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uptc.DatosDePrueba;

/** Reglas de cálculo que viven en el modelo. */
class ModeloTest {

    @Test
    void elPrecioFinalAplicaElPorcentajeDeDescuento() {
        Producto producto = DatosDePrueba.producto("1", "Lámpara", "Hogar", "Luma", 200_000, 5);

        assertEquals(200_000, producto.precioFinal(), 0.01);

        producto.setDescuento(25);
        assertEquals(150_000, producto.precioFinal(), 0.01);
    }

    @Test
    void elSubtotalDeUnaLineaEsPrecioFinalPorCantidad() {
        Producto producto = DatosDePrueba.producto("1", "Lámpara", "Hogar", "Luma", 200_000, 5);
        producto.setDescuento(25);

        assertEquals(450_000, new ItemCarrito(producto, 3).getSubtotal(), 0.01);
        assertEquals(0, new ItemCarrito().getSubtotal());
    }

    @Test
    void cadaTipoDeInteraccionTieneUnPesoSegunElInteresQueDemuestra() {
        assertEquals(1, TipoInteraccion.VISTA.getPeso());
        assertEquals(10, TipoInteraccion.COMPRA.getPeso());
    }
}
