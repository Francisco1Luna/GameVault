package gui;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import negocio.AuthService;

public class CadastroController {

    private final AuthService auth = Contexto.get().getAuthService();

    @FXML
    private TextField campoNome;

    @FXML
    private TextField campoCpf;

    @FXML
    private DatePicker campoNascimento;

    @FXML
    private TextField campoEmail;

    @FXML
    private TextField campoTelefone;

    @FXML
    private PasswordField campoSenha;

    @FXML
    private PasswordField campoConfirmarSenha;

    @FXML
    private CheckBox checkAssinante;

    @FXML
    private void aoCadastrar() {
        if (!campoSenha.getText().equals(campoConfirmarSenha.getText())) {
            Telas.erro("A confirmação de senha não confere.");
            return;
        }
        try {
            auth.cadastrar(campoNome.getText(), campoCpf.getText(), campoNascimento.getValue(),
                    campoEmail.getText(), campoTelefone.getText(), campoSenha.getText(),
                    checkAssinante.isSelected());
            auth.login(campoCpf.getText(), campoSenha.getText()); // entra direto depois de criar a conta
            Telas.trocar(campoNome, "/gui/view/Principal.fxml");
        } catch (IllegalArgumentException e) {
            Telas.erro(e.getMessage());
        }
    }

    @FXML
    private void aoVoltar() {
        Telas.trocar(campoNome, "/gui/view/Login.fxml");
    }
}
