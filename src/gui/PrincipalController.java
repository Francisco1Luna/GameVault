package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class PrincipalController {
    @FXML
    private BorderPane painelPrincipal;

    private void carregarTela(String caminho) {
        try {
            Parent raiz = FXMLLoader.load(getClass().getResource(caminho));
            painelPrincipal.setCenter(raiz);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
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
