package gui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

/** Alertas com o mesmo tema (CSS) da janela principal. */
public final class Alertas {

    // botões em português (os padrões do JavaFX saem em inglês: "OK" / "Cancel")
    public static final ButtonType CONFIRMAR = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
    public static final ButtonType CANCELAR = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

    private Alertas() { }

    public static void informar(String mensagem) {
        criar(Alert.AlertType.INFORMATION, "Informação", mensagem).showAndWait();
    }

    public static void avisar(String mensagem) {
        criar(Alert.AlertType.WARNING, "Atenção", mensagem).showAndWait();
    }

    public static void erro(String mensagem) {
        criar(Alert.AlertType.ERROR, "Erro", mensagem).showAndWait();
    }

    public static boolean confirmar(String titulo, String mensagem) {
        Alert alerta = criar(Alert.AlertType.CONFIRMATION, titulo, mensagem);
        alerta.getButtonTypes().setAll(CONFIRMAR, CANCELAR);
        return alerta.showAndWait().orElse(CANCELAR) == CONFIRMAR;
    }

    private static Alert criar(Alert.AlertType tipo, String titulo, String mensagem) {
        // getMessage() de uma exceção pode ser null: nunca mostra um alerta vazio ou "null"
        String texto = mensagem == null || mensagem.trim().isEmpty() ? "Ocorreu um erro inesperado." : mensagem;
        Alert alerta = new Alert(tipo, texto);
        alerta.setTitle("GameVault");
        alerta.setHeaderText(titulo);
        aplicarTema(alerta);
        return alerta;
    }

    /** Copia o CSS (tema claro/escuro) da janela principal para um alerta ou diálogo qualquer. */
    public static void aplicarTema(Dialog<?> dialogo) {
        for (Window janela : Window.getWindows()) {
            if (janela.isShowing() && janela.getScene() != null) {
                dialogo.getDialogPane().getStylesheets().setAll(janela.getScene().getStylesheets());
                break;
            }
        }
    }
}