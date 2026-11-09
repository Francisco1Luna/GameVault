package gui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

/** Alertas com o mesmo tema (CSS) da janela principal. */
public final class Alertas {

    private Alertas() { }

    public static void informar(String mensagem) {
        criar(Alert.AlertType.INFORMATION, "Informação", mensagem).showAndWait();
    }

    public static void avisar(String mensagem) {
        criar(Alert.AlertType.WARNING, "Atenção", mensagem).showAndWait();
    }

    public static boolean confirmar(String titulo, String mensagem) {
        Alert alerta = criar(Alert.AlertType.CONFIRMATION, titulo, mensagem);
        return alerta.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private static Alert criar(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo, mensagem);
        alerta.setTitle("GameVault");
        alerta.setHeaderText(titulo);
        // copia o CSS da janela principal para o alerta
        for (Window janela : Window.getWindows()) {
            if (janela.isShowing() && janela.getScene() != null) {
                alerta.getDialogPane().getStylesheets().setAll(janela.getScene().getStylesheets());
                break;
            }
        }
        return alerta;
    }
}