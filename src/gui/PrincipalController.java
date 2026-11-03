package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import model.Cliente;
import model.ClienteAssinante;
import negocio.AuthService;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class PrincipalController {
    @FXML
    private BorderPane painelPrincipal;

    @FXML
    private Label rotuloUsuario;

    private final AuthService auth = Contexto.get().getAuthService();

    @FXML
    public void initialize() {
        rotuloUsuario.setText(auth.getUsuarioAtual().map(PrincipalController::descrever)
                .orElse("Nenhum usuário logado"));
    }

    private static String descrever(Cliente cliente) {
        if (cliente instanceof ClienteAssinante) {
            double desconto = ((ClienteAssinante) cliente).getPercentualDesconto();
            return "Olá, " + cliente.getNome() + " - Assinante (" + String.format("%.0f", desconto) + "% de desconto nas compras)";
        }
        return "Olá, " + cliente.getNome() + " - Cliente comum";
    }

    @FXML
    private void sair() {
        auth.logout();
        Telas.trocar(painelPrincipal, "/gui/view/Login.fxml");
    }

    private void carregarTela(String caminho) {
        try {
            Parent raiz = FXMLLoader.load(getClass().getResource(caminho));
            painelPrincipal.setCenter(raiz);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void abrirLoja() {
        carregarTela("/gui/view/Loja.fxml");
    }

    @FXML
    private void abrirPlataformas() {
        carregarTela("/gui/view/Plataformas.fxml");
    }

    @FXML
    private void abrirJogos() {
        carregarTela("/gui/view/Jogos.fxml"); //o metodo que eu escrevi serve para qualquer um
    }


}
