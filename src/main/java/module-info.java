module uptc {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens uptc to javafx.fxml, com.fasterxml.jackson.databind;
    opens uptc.model to com.fasterxml.jackson.databind;
    opens uptc.dto to com.fasterxml.jackson.databind;

    exports uptc;
    exports uptc.model;
    exports uptc.repository;
    exports uptc.exception;
}