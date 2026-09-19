module uptc {
    requires javafx.controls;
    requires javafx.fxml;

    opens uptc to javafx.fxml;
    exports uptc;
}
