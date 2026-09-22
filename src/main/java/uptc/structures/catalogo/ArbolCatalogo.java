package uptc.structures.catalogo;

import uptc.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ArbolCatalogo {

    void insertar(Producto producto);

    Optional<Producto> buscar(String id);

    boolean eliminar(String id);

    List<Producto> obtenerInOrder();

    List<Producto> buscarPorRangoPrecio(double precioMinimo, double precioMaximo);

    void vaciar();

    int getTamano();

    boolean estaVacio();

    NodoArbolCatalogo getRaiz();
}