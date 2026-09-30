package uptc.viewController.components;

import java.time.LocalDateTime;
import java.util.UUID;
import uptc.model.Interaccion;
import uptc.model.TipoInteraccion;
import uptc.viewController.ShopContext;

/**
 * Registra en el historial de la sesión lo que hace el usuario activo sobre
 * un producto (clic, carrito...). El recomendador usa ese historial.
 */
public final class InteractionTracker {

    private InteractionTracker() {
    }

    public static void track(String productId, TipoInteraccion type) {
        ShopContext.interactions().add(new Interaccion(
                UUID.randomUUID().toString(),
                ShopContext.currentUser().getId(),
                productId,
                type,
                LocalDateTime.now(),
                type.getPeso()));
    }
}
