package uptc.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import uptc.exception.AutenticacionException;
import uptc.model.RolUsuario;
import uptc.model.Usuario;
import uptc.utils.I18n;
import uptc.utils.ThemeManager;

/** Pantalla de inicio de sesión, con selector de idioma y de tema. */
public class LoginViewController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private ComboBox<String> languageBox;

    @FXML
    private void initialize() {
        languageBox.getItems().setAll("ES", "EN");
        languageBox.getSelectionModel().select(I18n.language().toUpperCase());
        // Los textos del FXML se traducen al cargarlo: al cambiar de idioma se recarga la pantalla.
        languageBox.valueProperty().addListener((observable, oldLanguage, newLanguage) -> {
            I18n.setLanguage(newLanguage);
            ViewNavigator.go("login");
        });
    }

    @FXML
    private void toggleTheme() {
        ThemeManager.getInstance().toggle();
    }

    @FXML
    private void login() {
        try {
            ShopContext.setCurrentUser(ShopContext.users().login(emailField.getText(), passwordField.getText()));
            ViewNavigator.go("store");
        } catch (AutenticacionException e) {
            errorLabel.setText(I18n.text("login.invalid"));
        }
    }

    /** Entra sin contraseña con el primer usuario CLIENTE (modo demostración). */
    @FXML
    private void guest() {
        for (Usuario user : ShopContext.users().findAll()) {
            if (user.getRol() == RolUsuario.CLIENTE) {
                ShopContext.setCurrentUser(user);
                break;
            }
        }
        ViewNavigator.go("store");
    }

    @FXML
    private void register() {
        ViewNavigator.go("register");
    }
}
