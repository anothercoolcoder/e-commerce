package uptc.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import uptc.exception.CarritoVacioException;
import uptc.exception.PersistenceException;
import uptc.exception.StockInsuficienteException;
import uptc.exception.ValidationException;
import uptc.model.Compra;
import uptc.model.DetalleCompra;
import uptc.model.ItemCarrito;
import uptc.model.Producto;
import uptc.model.RolUsuario;
import uptc.model.Usuario;
import uptc.persistence.CompraJsonDao;
import uptc.utils.I18n;

/** Checkout simulado: valida el carrito, descuenta stock y registra el pedido en JSON. */
public class CompraController {
    private final ProductoController products;
    private final CompraJsonDao dao;
    private final List<Compra> purchases;

    /** Carga los pedidos existentes desde el DAO. */
    public CompraController(ProductoController products, CompraJsonDao dao) throws PersistenceException {
        this.products = products;
        this.dao = dao;
        this.purchases = new ArrayList<>(dao.load());
    }

    /** Confirma la compra del carrito para el usuario, guarda el pedido y vacía el carrito. */
    public Compra checkout(String userId, CarritoController cart) throws PersistenceException {
        if (cart.getItems().isEmpty()) {
            throw new CarritoVacioException(I18n.text("error.cartEmpty"));
        }
        // Primero se revisa todo el carrito, para no descontar stock de un pedido que no se puede completar.
        for (ItemCarrito item : cart.getItems()) {
            Producto product = products.find(item.getProducto().getId());
            if (item.getCantidad() > product.getStock()) {
                throw new StockInsuficienteException(I18n.format("error.stock", product.getNombre()));
            }
        }
        List<DetalleCompra> details = new ArrayList<>();
        for (ItemCarrito item : cart.getItems()) {
            Producto product = item.getProducto();
            products.reduceStock(product.getId(), item.getCantidad());
            // Se guarda el precio pagado: si el producto cambia de precio, el pedido no cambia.
            details.add(new DetalleCompra(product.getId(), item.getCantidad(), product.precioFinal()));
        }
        Compra purchase = new Compra(UUID.randomUUID().toString(), userId, LocalDateTime.now(), details, cart.total());
        purchases.add(purchase);
        dao.save(purchases);
        cart.clear();
        return purchase;
    }

    /** Devuelve los pedidos de un usuario. */
    public List<Compra> history(String userId) {
        List<Compra> result = new ArrayList<>();
        for (Compra purchase : purchases) {
            if (purchase.getUsuarioId().equals(userId)) {
                result.add(purchase);
            }
        }
        return result;
    }

    /**
     * Historial de un usuario consultado por otra persona: cada quien puede
     * ver su propio historial, pero solo un ADMIN puede ver el de los demás.
     */
    public List<Compra> historyFor(Usuario requester, String userId) throws ValidationException {
        boolean ownHistory = requester != null && requester.getId().equals(userId);
        boolean admin = requester != null && requester.getRol() == RolUsuario.ADMIN;
        if (!ownHistory && !admin) {
            throw new ValidationException(I18n.text("error.historyForbidden"));
        }
        return history(userId);
    }

    /** Devuelve una copia de todos los pedidos. */
    public List<Compra> all() {
        return List.copyOf(purchases);
    }
}
