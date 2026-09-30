package uptc.viewController;

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import uptc.controller.EstadisticasController;
import uptc.controller.ProductoController;
import uptc.exception.PersistenceException;
import uptc.exception.ValidationException;
import uptc.model.Categoria;
import uptc.model.Compra;
import uptc.model.Producto;
import uptc.model.Usuario;
import uptc.utils.Formato;
import uptc.utils.I18n;
import uptc.viewController.components.ProductEmoji;
import uptc.viewController.components.ProductFormDialog;
import uptc.viewController.components.PurchaseList;

/**
 * Panel de administración. Pestaña de inventario (buscar, filtrar, crear,
 * editar, eliminar, cambiar stock, desactivar y reactivar productos) y pestaña
 * de compras por usuario. La pestaña de estadísticas tiene su propio
 * controlador ({@link StatisticsViewController}).
 */
public class AdminViewController {
    // Pestaña de inventario
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private TableView<Producto> table;
    @FXML private TableColumn<Producto, String> emoji, sku, product, category, price, stockColumn, state;
    @FXML private TextField stock;
    @FXML private Label status;

    // Pestaña de compras por usuario
    @FXML private ComboBox<String> userBox;
    @FXML private Label userSummary;
    @FXML private ScrollPane historyPane;

    private final ProductoController products = ShopContext.products();
    private List<Usuario> users;
    private PurchaseList history;

    @FXML
    private void initialize() {
        emoji.setCellValueFactory(cell -> new SimpleStringProperty(ProductEmoji.of(cell.getValue())));
        sku.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSku()));
        product.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombre()));
        category.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategoriaId()));
        price.setCellValueFactory(cell -> new SimpleStringProperty(Formato.moneda(cell.getValue().getPrecio())));
        stockColumn.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf((int) cell.getValue().getStock())));
        state.setCellValueFactory(cell -> new SimpleStringProperty(
                I18n.text(cell.getValue().isActivo() ? "admin.active" : "admin.inactive")));

        // El campo de stock solo acepta dígitos: no se pueden escribir decimales, signos ni letras.
        ProductFormDialog.onlyDigits(stock);

        // La tabla se filtra mientras se escribe en el buscador o al cambiar de categoría.
        searchField.textProperty().addListener((observable, oldText, newText) -> showProducts());
        loadCategories();
        categoryBox.valueProperty().addListener((observable, oldCategory, newCategory) -> showProducts());
        showProducts();

        history = new PurchaseList(products);
        historyPane.setContent(history);
        users = ShopContext.users().findAll();
        for (Usuario user : users) {
            userBox.getItems().add(user.getNombre() + " · " + user.getCorreo());
        }
        userBox.getSelectionModel().selectFirst();
        userBox.valueProperty().addListener((observable, oldUser, newUser) -> showHistory());
        showHistory();
    }

    // ------------------------------------------------------------ inventario: búsqueda y filtro

    /** Llena el selector con "Todas" y las categorías del catálogo, conservando la que estaba elegida. */
    private void loadCategories() {
        String selected = categoryBox.getValue();
        categoryBox.getItems().setAll(I18n.text("store.allCategories"));
        categoryBox.getItems().addAll(products.categoryNames());
        if (selected != null && categoryBox.getItems().contains(selected)) {
            categoryBox.setValue(selected);
        } else {
            categoryBox.getSelectionModel().selectFirst();
        }
    }

    /** Muestra en la tabla los productos que cumplen el texto del buscador y la categoría elegida. */
    private void showProducts() {
        String selected = categoryBox.getValue();
        boolean allCategories = selected == null || selected.equals(I18n.text("store.allCategories"));
        String selectedCategory = allCategories ? null : selected;
        table.getItems().setAll(products.search(searchField.getText(), selectedCategory));
    }

    // ------------------------------------------------------------ inventario: crear, editar, eliminar

    @FXML
    private void newProduct() {
        ProductFormDialog form = new ProductFormDialog(null, products.nextId(), products.categoryNames(),
                newProduct -> {
                    products.create(ShopContext.currentUser(), newProduct);
                    registerCategory(newProduct.getCategoriaId());
                });
        if (saved(form)) {
            status.setText(I18n.text("admin.created"));
        }
    }

    @FXML
    private void editProduct() {
        Producto selected = selectedProduct();
        if (selected == null) {
            return;
        }
        ProductFormDialog form = new ProductFormDialog(selected, selected.getId(), products.categoryNames(),
                edited -> {
                    products.update(ShopContext.currentUser(), edited);
                    registerCategory(edited.getCategoriaId());
                });
        if (saved(form)) {
            status.setText(I18n.text("admin.updated"));
        }
    }

    @FXML
    private void deleteProduct() {
        Producto selected = selectedProduct();
        if (selected == null) {
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                I18n.format("admin.deleteConfirm", selected.getNombre()), ButtonType.OK, ButtonType.CANCEL);
        confirmation.setHeaderText(null);
        if (!confirmed(confirmation)) {
            return;
        }
        try {
            products.delete(ShopContext.currentUser(), selected.getId());
            status.setText(I18n.text("admin.deleted"));
        } catch (ValidationException | PersistenceException e) {
            status.setText(e.getMessage());
        }
        loadCategories();
        showProducts();
    }

    /** Muestra el formulario y, si se guardó, actualiza la tabla y el filtro de categorías. */
    private boolean saved(ProductFormDialog form) {
        if (!confirmed(form)) {
            return false;
        }
        loadCategories();
        showProducts();
        return true;
    }

    /** Muestra un diálogo con los estilos de la aplicación y devuelve true si se aceptó. */
    private boolean confirmed(Dialog<ButtonType> dialog) {
        dialog.getDialogPane().getStylesheets().setAll(table.getScene().getStylesheets());
        ButtonType answer = dialog.showAndWait().orElse(ButtonType.CANCEL);
        return answer.getButtonData().isDefaultButton();
    }

    /** Si el producto trae una categoría nueva, se registra para que aparezca en el filtro de la tienda. */
    private void registerCategory(String name) throws ValidationException {
        if (!name.isBlank() && ShopContext.categories().find(name) == null) {
            ShopContext.categories().create(ShopContext.currentUser(), new Categoria(name, name, ""));
        }
    }

    // ------------------------------------------------------------ inventario: stock y estado

    @FXML
    private void saveStock() {
        Producto selected = selectedProduct();
        if (selected == null) {
            return;
        }
        double previousStock = selected.getStock();
        try {
            selected.setStock(Integer.parseInt(stock.getText().trim()));
            products.update(ShopContext.currentUser(), selected);
            status.setText(I18n.text("admin.stockSaved"));
            stock.clear();
        } catch (NumberFormatException e) {
            status.setText(I18n.text("error.stockInteger"));
        } catch (ValidationException | PersistenceException e) {
            // La operación fue rechazada: el producto conserva el stock que tenía.
            selected.setStock(previousStock);
            status.setText(e.getMessage());
        }
        table.refresh();
    }

    @FXML
    private void deactivate() {
        Producto selected = selectedProduct();
        if (selected == null) {
            return;
        }
        try {
            products.deactivate(ShopContext.currentUser(), selected.getId());
            status.setText(I18n.text("admin.deactivated"));
        } catch (ValidationException | PersistenceException e) {
            status.setText(e.getMessage());
        }
        table.refresh();
    }

    @FXML
    private void activate() {
        Producto selected = selectedProduct();
        if (selected == null) {
            return;
        }
        try {
            products.activate(ShopContext.currentUser(), selected.getId());
            status.setText(I18n.text("admin.activated"));
        } catch (ValidationException | PersistenceException e) {
            status.setText(e.getMessage());
        }
        table.refresh();
    }

    /** Producto seleccionado en la tabla; si no hay ninguno avisa y devuelve null. */
    private Producto selectedProduct() {
        Producto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            status.setText(I18n.text("admin.select"));
        }
        return selected;
    }

    // ------------------------------------------------------------ compras por usuario

    /** Muestra el historial detallado del usuario elegido en el selector. */
    private void showHistory() {
        Usuario selected = users.get(userBox.getSelectionModel().getSelectedIndex());
        try {
            List<Compra> purchases = ShopContext.purchases().historyFor(ShopContext.currentUser(), selected.getId());
            double spent = new EstadisticasController().totalSales(purchases);
            userSummary.setText(I18n.format("history.summary", purchases.size(), Formato.moneda(spent)));
            history.show(purchases);
        } catch (ValidationException e) {
            // El controlador de negocio rechaza la consulta si quien la hace no es ADMIN.
            userSummary.setText(e.getMessage());
            history.show(List.of());
        }
    }

    @FXML
    private void back() {
        ViewNavigator.go("store");
    }
}
