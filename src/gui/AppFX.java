package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppFX extends Application {

    @Override
    public void start(Stage palco) throws Exception {

        Contexto.get().getAuthService().garantirAdministradorPadrao();
        DadosExemplo.carregar();

        Parent raiz = FXMLLoader.load(
                getClass().getResource("/gui/view/Login.fxml")
        );

        Scene cena = new Scene(raiz, 900, 600);
        Temas.aplicar(cena);
        palco.setScene(cena);
        palco.setTitle("GameVault");
        palco.setMaximized(true);
        palco.show();
    }

}
