package uptc.viewController;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.fxml.FXML;
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
    private static final String ALL_CATEGORIES = "Todas";
    private static final String SORT_NAME = "Nombre";
    private static final String SORT_PRICE_ASC = "Precio menor";
    private static final String SORT_PRICE_DESC = "Precio mayor";
    private static final String SORT_RATING = "Mejor calificados";
    private static final int RECOMMENDATION_COUNT = 4;

    @FXML private TextField searchField;
    @FXML private FlowPane productGrid, recommendedGrid;
    @FXML private ComboBox<String> categoryBox, sortBox, languageBox;
    @FXML private Label cartBadge, userLabel, emptyLabel, statusLabel, storeTitle, recommendedReason;
    @FXML private CheckBox availableOnly;

    /** Observer del carrito: actualiza el contador cada vez que el carrito cambia. */
    private final PropertyChangeListener cartListener = event -> updateCartBadge();

    @FXML
    private void initialize() {
        userLabel.setText(ShopContext.currentUser().getNombre());

        categoryBox.getItems().add(ALL_CATEGORIES);
        for (Categoria category : ShopContext.categories().findAll()) {
            categoryBox.getItems().add(category.getId());
        }
        categoryBox.getSelectionModel().selectFirst();
        categoryBox.setOnAction(event -> render());

        sortBox.getItems().setAll(SORT_NAME, SORT_PRICE_ASC, SORT_PRICE_DESC, SORT_RATING);
        sortBox.getSelectionModel().selectFirst();
        sortBox.setOnAction(event -> render());

        languageBox.getItems().setAll("ES", "EN", "PT");
        languageBox.getSelectionModel().select(I18n.language().toUpperCase());
        languageBox.setOnAction(event -> {
            I18n.setLanguage(languageBox.getValue());
            translate();
        });

        ShopContext.cart().addListener(cartListener);
        translate();
        renderRecommendations();
        render();
        updateCartBadge();
    }

    private void translate() {
        searchField.setPromptText(I18n.text("store.search"));
        storeTitle.setText(I18n.text("store.discover"));
        emptyLabel.setText(I18n.text("store.empty"));
        availableOnly.setText(I18n.text("store.available"));
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
    private void logout() {
        leaveTo("login");
    }

    @FXML
    private void openAdmin() {
        if (ShopContext.currentUser().getRol() == RolUsuario.ADMIN) {
            leaveTo("admin");
        } else {
            statusLabel.setText("Solo ADMIN puede abrir este panel");
        }
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
        String category = ALL_CATEGORIES.equals(categoryBox.getValue()) ? null : categoryBox.getValue();
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
        return switch (sortBox.getValue()) {
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
            statusLabel.setText("✓ Producto agregado al carrito");
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
