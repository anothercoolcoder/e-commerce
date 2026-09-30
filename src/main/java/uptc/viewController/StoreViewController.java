package uptc.viewController;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import uptc.model.Categoria;
import uptc.model.ItemCarrito;
import uptc.model.Producto;
import uptc.model.RolUsuario;
import uptc.model.TipoInteraccion;
import uptc.utils.I18n;
import uptc.utils.ThemeManager;
import uptc.viewController.components.InteractionTracker;
import uptc.viewController.components.ProductCard;

/** Pantalla principal de la tienda: recomendaciones, búsqueda, filtros y catálogo. */
public class StoreViewController {
    private static final int RECOMMENDATION_COUNT = 4;
    // Posiciones de las opciones del selector de orden (el texto cambia con el idioma).
    private static final int SORT_PRICE_ASC = 1;
    private static final int SORT_PRICE_DESC = 2;
    private static final int SORT_RATING = 3;

    @FXML private TextField searchField;
    @FXML private FlowPane productGrid, recommendedGrid;
    @FXML private ComboBox<String> categoryBox, sortBox, languageBox;
    @FXML private Label cartBadge, userLabel, emptyLabel, statusLabel, recommendedReason;
    @FXML private CheckBox availableOnly;
    @FXML private Button adminButton;

    /** Observer del carrito: actualiza el contador cada vez que el carrito cambia. */
    private final PropertyChangeListener cartListener = event -> updateCartBadge();

    @FXML
    private void initialize() {
        userLabel.setText(ShopContext.currentUser().getNombre());

        // El botón del panel de administración solo existe para los ADMIN.
        boolean admin = ShopContext.currentUser().getRol() == RolUsuario.ADMIN;
        adminButton.setVisible(admin);
        adminButton.setManaged(admin);

        categoryBox.getItems().add(I18n.text("store.allCategories"));
        for (Categoria category : ShopContext.categories().findAll()) {
            categoryBox.getItems().add(category.getId());
        }
        categoryBox.getSelectionModel().selectFirst();
        categoryBox.valueProperty().addListener((observable, oldCategory, newCategory) -> render());

        sortBox.getItems().setAll(I18n.text("store.sort.name"), I18n.text("store.sort.priceAsc"),
                I18n.text("store.sort.priceDesc"), I18n.text("store.sort.rating"));
        sortBox.getSelectionModel().selectFirst();
        sortBox.valueProperty().addListener((observable, oldOrder, newOrder) -> render());

        languageBox.getItems().setAll("ES", "EN");
        languageBox.getSelectionModel().select(I18n.language().toUpperCase());
        // Los textos del FXML se traducen al cargarlo: al cambiar de idioma se recarga la pantalla.
        languageBox.valueProperty().addListener((observable, oldLanguage, newLanguage) -> {
            I18n.setLanguage(newLanguage);
            leaveTo("store");
        });

        ShopContext.cart().addListener(cartListener);
        renderRecommendations();
        render();
        updateCartBadge();
    }

    // ------------------------------------------------------------ acciones del FXML

    @FXML
    private void search() {
        render();
    }

    @FXML
    private void clearFilters() {
        searchField.clear();
        categoryBox.getSelectionModel().selectFirst();
        availableOnly.setSelected(false);
        render();
    }

    @FXML
    private void toggleTheme() {
        ThemeManager.getInstance().toggle();
    }

    @FXML
    private void openCart() {
        leaveTo("cart");
    }

    @FXML
    private void openAccount() {
        leaveTo("account");
    }

    @FXML
    private void openAdmin() {
        leaveTo("admin");
    }

    @FXML
    private void logout() {
        leaveTo("login");
    }

    /** Al salir de la pantalla se retira el listener para que el carrito no conserve vistas viejas. */
    private void leaveTo(String view) {
        ShopContext.cart().removeListener(cartListener);
        ViewNavigator.go(view);
    }

    // ------------------------------------------------------------ dibujo

    /** Muestra los productos que recomienda el árbol de decisión para el usuario activo. */
    private void renderRecommendations() {
        String userId = ShopContext.currentUser().getId();
        recommendedGrid.getChildren().clear();
        for (Producto product : ShopContext.recommendations().forUser(userId, RECOMMENDATION_COUNT)) {
            recommendedGrid.getChildren().add(createCard(product));
        }
        recommendedReason.setText(ShopContext.recommendations().explain(userId));
    }

    /** Consulta el catálogo con el texto y los filtros actuales y dibuja una tarjeta por producto. */
    private void render() {
        String selected = categoryBox.getValue();
        boolean allCategories = selected == null || selected.equals(I18n.text("store.allCategories"));
        String category = allCategories ? null : selected;
        List<Producto> products = new ArrayList<>(ShopContext.products()
                .filter(searchField.getText(), category, null, availableOnly.isSelected()));
        products.sort(comparator());

        productGrid.getChildren().clear();
        for (Producto product : products) {
            productGrid.getChildren().add(createCard(product));
        }
        emptyLabel.setVisible(products.isEmpty());
    }

    private Comparator<Producto> comparator() {
        return switch (sortBox.getSelectionModel().getSelectedIndex()) {
            case SORT_PRICE_ASC -> Comparator.comparingDouble(Producto::precioFinal);
            case SORT_PRICE_DESC -> Comparator.comparingDouble(Producto::precioFinal).reversed();
            case SORT_RATING -> Comparator.comparingDouble(Producto::getCalificacion).reversed();
            default -> Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER);
        };
    }

    private ProductCard createCard(Producto product) {
        return new ProductCard(product, () -> openDetail(product), () -> addToCart(product));
    }

    private void openDetail(Producto product) {
        InteractionTracker.track(product.getId(), TipoInteraccion.CLIC);
        ShopContext.setSelectedProduct(product);
        leaveTo("detail");
    }

    private void addToCart(Producto product) {
        try {
            ShopContext.cart().add(product.getId(), 1);
            InteractionTracker.track(product.getId(), TipoInteraccion.CARRITO);
            statusLabel.setText(I18n.text("store.added"));
        } catch (RuntimeException e) {
            // Stock insuficiente o producto inactivo: se muestra el motivo al usuario.
            statusLabel.setText(e.getMessage());
        }
    }

    private void updateCartBadge() {
        int units = 0;
        for (ItemCarrito item : ShopContext.cart().getItems()) {
            units += item.getCantidad();
        }
        cartBadge.setText(String.valueOf(units));
    }
}
