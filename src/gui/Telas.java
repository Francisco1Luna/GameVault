package gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;

import java.io.IOException;

/** Navegação entre telas inteiras (Login, Cadastro, Principal) e alertas comuns. */
public final class Telas {

    private Telas() { }

    /** Troca a tela inteira da janela em que o componente está. */
    public static void trocar(Node origem, String caminhoFxml) {
        try {
            Parent raiz = FXMLLoader.load(Telas.class.getResource(caminhoFxml));
            origem.getScene().setRoot(raiz);
        } catch (IOException e) {
            erro("Não foi possível abrir a tela: " + e.getMessage());
        }
    }

    public static void erro(String mensagem) {
        new Alert(Alert.AlertType.ERROR, mensagem).showAndWait();
    }
}
