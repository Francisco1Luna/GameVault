package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TesteController {

    @FXML
    private Label mensagem;

    @FXML
    private void aoClicar() {
        mensagem.setText("Controller funcionando!");
    }
}