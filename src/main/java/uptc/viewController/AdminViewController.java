package uptc.viewController;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import uptc.exception.PersistenceException;
import uptc.exception.ValidationException;
import uptc.model.Producto;

/** Panel de administración: actualizar el stock y desactivar productos. */
public class AdminViewController {
    @FXML private TableView<Producto> table;
    @FXML private TableColumn<Producto, String> id, product, stockColumn, state;
    @FXML private TextField stock;
    @FXML private Label status;

    @FXML
    private void initialize() {
        id.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSku()));
        product.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombre()));
        stockColumn.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf((int) cell.getValue().getStock())));
        state.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().isActivo() ? "Activo" : "Inactivo"));
        table.getItems().setAll(ShopContext.products().findAll());
    }

    @FXML
    private void saveStock() {
        Producto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            status.setText("Seleccione un producto");
            return;
        }
        double previousStock = selected.getStock();
        try {
            selected.setStock(Double.parseDouble(stock.getText().trim()));
            ShopContext.products().update(ShopContext.currentUser(), selected);
            status.setText("Stock actualizado");
        } catch (NumberFormatException e) {
            status.setText("El stock debe ser un número");
        } catch (ValidationException | PersistenceException e) {
            // La operación fue rechazada: el producto conserva el stock que tenía.
            selected.setStock(previousStock);
            status.setText(e.getMessage());
        }
        table.refresh();
    }

    @FXML
    private void deactivate() {
        Producto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            status.setText("Seleccione un producto");
            return;
        }
        try {
            ShopContext.products().delete(ShopContext.currentUser(), selected.getId());
            status.setText("Producto desactivado: ya no aparece en la tienda");
        } catch (ValidationException | PersistenceException e) {
            status.setText(e.getMessage());
        }
        table.refresh();
    }

    @FXML
    private void openStatistics() {
        ViewNavigator.go("statistics");
    }

    @FXML
    private void back() {
        ViewNavigator.go("store");
    }
}
