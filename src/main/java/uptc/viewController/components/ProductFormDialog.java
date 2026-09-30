package uptc.viewController.components;

import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import uptc.exception.PersistenceException;
import uptc.exception.ValidationException;
import uptc.model.Producto;
import uptc.utils.I18n;

/**
 * Formulario para crear o editar un producto con todos sus datos, incluido el
 * emoji. El formulario solo reúne los datos: quien lo abre le entrega la
 * acción de guardar ({@link Saver}). Si guardar falla (dato inválido, SKU
 * repetido...) el mensaje se muestra dentro del formulario y este no se cierra.
 */
public class ProductFormDialog extends Dialog<ButtonType> {

    /** Acción que guarda el producto del formulario; la implementa el panel de administración. */
    @FunctionalInterface
    public interface Saver {
        void save(Producto product) throws ValidationException, PersistenceException;
    }

    private final TextField id = new TextField();
    private final TextField sku = new TextField();
    private final TextField name = new TextField();
    private final TextField description = new TextField();
    private final ComboBox<String> category = new ComboBox<>();
    private final TextField brand = new TextField();
    private final TextField price = new TextField();
    private final TextField discount = new TextField("0");
    private final TextField stock = new TextField("0");
    private final TextField rating = new TextField("0");
    private final ComboBox<String> emoji = new ComboBox<>();
    private final CheckBox active = new CheckBox(I18n.text("form.active"));
    private final Label error = new Label();

    private final Producto existing;
    private int rows;

    /**
     * @param existing    producto a editar, o null para crear uno nuevo
     * @param suggestedId id propuesto para un producto nuevo
     * @param categories  categorías existentes (se puede escribir una nueva)
     * @param saver       acción que guarda el producto
     */
    public ProductFormDialog(Producto existing, String suggestedId, List<String> categories, Saver saver) {
        this.existing = existing;
        setTitle(I18n.text(existing == null ? "form.title.new" : "form.title.edit"));

        category.getItems().setAll(categories);
        category.setEditable(true);
        emoji.getItems().setAll(ProductEmoji.OPTIONS);
        emoji.setEditable(true);
        emoji.setPromptText(I18n.text("form.emojiHint"));
        onlyDigits(stock);
        error.getStyleClass().add("error");
        error.setWrapText(true);

        if (existing == null) {
            id.setText(suggestedId);
            active.setSelected(true);
        } else {
            fillFrom(existing);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        addRow(grid, "form.id", id);
        addRow(grid, "form.sku", sku);
        addRow(grid, "form.name", name);
        addRow(grid, "form.description", description);
        addRow(grid, "form.category", category);
        addRow(grid, "form.brand", brand);
        addRow(grid, "form.price", price);
        addRow(grid, "form.discount", discount);
        addRow(grid, "form.stock", stock);
        addRow(grid, "form.rating", rating);
        addRow(grid, "form.emoji", emoji);
        grid.add(active, 1, rows++);
        grid.add(error, 0, rows, 2, 1);
        getDialogPane().setContent(grid);

        ButtonType saveType = new ButtonType(I18n.text("form.save"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.text("form.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().setAll(saveType, cancelType);

        // Al pulsar Guardar se intenta guardar; si falla, se cancela el cierre del formulario.
        Button saveButton = (Button) getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                saver.save(readProduct());
            } catch (NumberFormatException e) {
                error.setText(I18n.text("form.numbers"));
                event.consume();
            } catch (ValidationException | PersistenceException e) {
                error.setText(e.getMessage());
                event.consume();
            }
        });
    }

    /** Hace que un campo de texto solo acepte dígitos (números enteros no negativos). */
    public static void onlyDigits(TextField field) {
        field.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));
    }

    private void addRow(GridPane grid, String labelKey, Node field) {
        grid.add(new Label(I18n.text(labelKey)), 0, rows);
        grid.add(field, 1, rows);
        rows++;
    }

    private void fillFrom(Producto product) {
        id.setText(product.getId());
        id.setDisable(true); // el id identifica al producto: no se cambia al editar
        sku.setText(product.getSku());
        name.setText(product.getNombre());
        description.setText(product.getDescripcion());
        category.setValue(product.getCategoriaId());
        brand.setText(product.getMarca());
        price.setText(number(product.getPrecio()));
        discount.setText(number(product.getDescuento()));
        stock.setText(String.valueOf((int) product.getStock()));
        rating.setText(number(product.getCalificacion()));
        emoji.setValue(product.getEmoji());
        active.setSelected(product.isActivo());
    }

    /** Construye el producto con lo escrito en el formulario. Lanza NumberFormatException si un número no es válido. */
    private Producto readProduct() {
        Producto product = new Producto(
                id.getText().trim(),
                sku.getText().trim(),
                name.getText().trim(),
                description.getText().trim(),
                decimal(price),
                decimal(discount),
                Integer.parseInt(stock.getText().trim()),
                text(category),
                brand.getText().trim(),
                decimal(rating),
                existing == null ? "" : existing.getImagen(),
                active.isSelected());
        product.setEmoji(text(emoji));
        return product;
    }

    /** Texto de un selector editable: lo que el usuario escribió o eligió. */
    private String text(ComboBox<String> box) {
        String value = box.getEditor().getText();
        return value == null ? "" : value.trim();
    }

    private double decimal(TextField field) {
        return Double.parseDouble(field.getText().trim().replace(',', '.'));
    }

    /** Muestra 320000 en lugar de 320000.0 cuando el número no tiene decimales. */
    private String number(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
