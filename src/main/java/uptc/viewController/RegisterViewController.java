package uptc.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import uptc.exception.PersistenceException;
import uptc.exception.ValidationException;
import uptc.model.RolUsuario;
import uptc.model.Usuario;

/** Pantalla de registro de clientes. */
public class RegisterViewController {
    @FXML private TextField nameField, emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void register() {
        try {
            String email = emailField.getText().trim();
            // El id del usuario se deriva de su correo, que es único.
            String id = email.replace("@", "-");
            Usuario user = ShopContext.users().register(id, nameField.getText(), email,
                    passwordField.getText(), RolUsuario.CLIENTE);
            ShopContext.setCurrentUser(user);
            ViewNavigator.go("store");
        } catch (ValidationException | PersistenceException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void back() {
        ViewNavigator.go("login");
    }
}
