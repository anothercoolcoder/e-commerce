module uptc {
    requires javafx.controls;
    requires javafx.fxml;

    opens uptc to javafx.fxml;
    exports uptc;
    exports uptc.model;
    exports uptc.dto;
    exports uptc.exception;
    exports uptc.repository;
    exports uptc.service;
    exports uptc.structures.catalogo;
    exports uptc.structures.decision;
}
