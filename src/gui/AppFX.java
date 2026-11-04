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

        Parent raiz = FXMLLoader.load(
                getClass().getResource("/gui/view/Login.fxml")
        );

        palco.setScene(new Scene(raiz, 900, 600));
        palco.setTitle("GameVault");
        palco.setMaximized(true);
        palco.show();
    }

}
