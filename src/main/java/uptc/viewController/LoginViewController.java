package uptc.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    @FXML private Label errorLabel, titleLabel, subtitleLabel;
    @FXML private Button loginButton, guestButton, registerButton;
    @FXML private ComboBox<String> languageBox;

    @FXML
    private void initialize() {
        languageBox.getItems().setAll("ES", "EN", "PT");
        languageBox.getSelectionModel().select(I18n.language().toUpperCase());
        languageBox.setOnAction(event -> {
            I18n.setLanguage(languageBox.getValue());
            translate();
        });
        translate();
    }

    private void translate() {
        titleLabel.setText(I18n.text("login.title"));
        subtitleLabel.setText(I18n.text("login.subtitle"));
        emailField.setPromptText(I18n.text("login.email"));
        passwordField.setPromptText(I18n.text("login.password"));
        loginButton.setText(I18n.text("login.enter"));
        guestButton.setText(I18n.text("login.guest"));
        registerButton.setText(I18n.text("login.register"));
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
