package gui;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import negocio.AuthService;

public class LoginController {

    private final AuthService auth = Contexto.get().getAuthService();

    @FXML
    private TextField campoCpf;

    @FXML
    private PasswordField campoSenha;

    @FXML
    private void aoEntrar() {
        try {
            auth.login(campoCpf.getText(), campoSenha.getText());
            Telas.trocar(campoCpf, "/gui/view/Principal.fxml");
        } catch (IllegalArgumentException e) {
            Telas.erro(e.getMessage());
            campoSenha.clear();
        }
    }

    @FXML
    private void aoCriarConta() {
        Telas.trocar(campoCpf, "/gui/view/Cadastro.fxml");
    }
}
