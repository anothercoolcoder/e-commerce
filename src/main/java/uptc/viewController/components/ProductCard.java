package uptc.viewController.components;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import uptc.model.Producto;
import uptc.utils.ImageLoader;

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
                createImageArea(),
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

    /** Imagen con las etiquetas de descuento / stock y el botón de favorito encima. */
    private StackPane createImageArea() {
        ImageView image = new ImageView(ImageLoader.loadProduct(product));
        image.setFitWidth(196);
        image.setFitHeight(196);
        image.setPreserveRatio(true);
        image.setSmooth(true);
        image.setAccessibleText(product.getNombre());

        HBox badges = new HBox(4);
        StackPane.setAlignment(badges, Pos.TOP_LEFT);
        if (soldOut()) {
            // Producto agotado: imagen atenuada y en escala de grises.
            image.setOpacity(.6);
            image.setEffect(new ColorAdjust(0, -1, 0, 0));
            badges.getChildren().add(new Badge("Agotado", "badge-sale"));
        } else {
            if (product.getDescuento() > 0) {
                badges.getChildren().add(new Badge("-" + (int) product.getDescuento() + "%", "badge-sale"));
            }
            if (product.getStock() <= LOW_STOCK) {
                badges.getChildren().add(new Badge("Últimas unidades", "badge-stock"));
            }
        }

        FavoriteButton favorite = new FavoriteButton(null);
        StackPane.setAlignment(favorite, Pos.TOP_RIGHT);

        StackPane area = new StackPane(image, badges, favorite);
        area.getStyleClass().add("product-image-frame");
        area.setPrefSize(196, 196);
        return area;
    }

    private Label createStockLabel() {
        String text;
        if (soldOut()) {
            text = "Sin stock";
        } else if (product.getStock() <= LOW_STOCK) {
            text = "Solo quedan " + (int) product.getStock();
        } else {
            text = "En stock";
        }
        Label stock = new Label(text);
        stock.getStyleClass().add(product.getStock() <= LOW_STOCK ? "stock-low" : "stock-ok");
        return stock;
    }

    private Button createCartButton(Runnable add) {
        Button cart = new Button(soldOut() ? "Sin stock" : "Agregar al carrito");
        cart.getStyleClass().add("btn-primary");
        cart.setMaxWidth(Double.MAX_VALUE);
        cart.setDisable(soldOut());
        cart.setAccessibleText("Agregar " + product.getNombre() + " al carrito");
        cart.setOnAction(event -> {
            add.run();
            // Confirmación visual breve; después el botón vuelve a su estado normal.
            cart.setText("✓ Agregado");
            cart.setDisable(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
            pause.setOnFinished(finished -> {
                cart.setText("Agregar al carrito");
                cart.setDisable(soldOut());
            });
            pause.play();
        });
        return cart;
    }
}
