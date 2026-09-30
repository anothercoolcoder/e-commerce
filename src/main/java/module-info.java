module uptc {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.prefs;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.datatype.jsr310;

    // JavaFX necesita acceder a la clase App para lanzar la aplicación.
    exports uptc;
    // FXMLLoader crea los controladores e inyecta los campos @FXML por reflexión.
    opens uptc.viewController to javafx.fxml;
    // Jackson lee y escribe los modelos por reflexión.
    opens uptc.model to com.fasterxml.jackson.databind;
}
