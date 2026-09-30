package uptc.viewController.components;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import uptc.model.Producto;
import uptc.utils.I18n;

/**
 * Tarjeta de un producto del catálogo. Las acciones (abrir el detalle y
 * agregar al carrito) se reciben por parámetro: la tarjeta solo dibuja.
 */
public class ProductCard extends VBox {
    private static final int LOW_STOCK = 5;

    private final Producto product;

    public ProductCard(Producto product, Runnable open, Runnable add) {
        this.product = product;
        getStyleClass().addAll("card", "product-card");
        setSpacing(8);
        setFocusTraversable(true);

        Label brand = new Label(product.getMarca() == null ? "" : product.getMarca().toUpperCase());
        brand.getStyleClass().add("product-brand");

        Label name = new Label(product.getNombre());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);
        name.setMaxHeight(38);
        name.setTooltip(new Tooltip(product.getNombre()));

        getChildren().addAll(
                createIllustration(),
                brand,
                name,
                new RatingStars(product.getCalificacion(), 0),
                new PriceLabel(product.getPrecio(), product.getDescuento()),
                createStockLabel(),
                createCartButton(add));

        // Un clic en cualquier parte de la tarjeta abre el detalle, salvo que sea sobre un botón.
        setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof Button)) {
                open.run();
            }
        });
    }

    public Producto getProduct() {
        return product;
    }

    private boolean soldOut() {
        return product.getStock() <= 0;
    }

    /** Emoji de la categoría con el botón de favorito encima. Sin texto: el nombre va debajo. */
    private StackPane createIllustration() {
        ProductEmoji emoji = new ProductEmoji(product);
        if (soldOut()) {
            emoji.setOpacity(.35);
        }
        FavoriteButton favorite = new FavoriteButton(null);
        StackPane.setAlignment(favorite, Pos.TOP_RIGHT);

        StackPane area = new StackPane(emoji, favorite);
        area.getStyleClass().add("product-image-frame");
        area.setPrefSize(196, 150);
        return area;
    }

    private Label createStockLabel() {
        String text;
        if (soldOut()) {
            text = I18n.text("card.soldOut");
        } else if (product.getStock() <= LOW_STOCK) {
            text = I18n.format("card.lowStock", (int) product.getStock());
        } else {
            text = I18n.text("card.inStock");
        }
        Label stock = new Label(text);
        stock.getStyleClass().add(product.getStock() <= LOW_STOCK ? "stock-low" : "stock-ok");
        return stock;
    }

    private Button createCartButton(Runnable add) {
        Button cart = new Button(I18n.text(soldOut() ? "card.soldOut" : "card.add"));
        cart.getStyleClass().add("btn-primary");
        cart.setMaxWidth(Double.MAX_VALUE);
        cart.setDisable(soldOut());
        cart.setOnAction(event -> {
            add.run();
            // Confirmación visual breve; después el botón vuelve a su estado normal.
            cart.setText(I18n.text("card.added"));
            cart.setDisable(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
            pause.setOnFinished(finished -> {
                cart.setText(I18n.text("card.add"));
                cart.setDisable(soldOut());
            });
            pause.play();
        });
        return cart;
    }
}
