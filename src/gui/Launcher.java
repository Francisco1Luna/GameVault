package gui;

/**
 * Ponto de entrada alternativo da interface.
 *
 * Quando a classe com o main estende javafx.application.Application (AppFX), o Java exige o JavaFX
 * no module-path e, sem isso, falha com "JavaFX runtime components are missing". Esta classe não
 * estende Application, então pode ser executada com o JavaFX apenas no classpath (caso do botão
 * Run do IntelliJ em projeto Maven/Gradle, ou de um jar). Ela só repassa a chamada para o AppFX,
 * que por isso NÃO tem main: sempre rode a interface por esta classe (gui.Launcher).
 */
public class Launcher {

    public static void main(String[] args) {
        javafx.application.Application.launch(AppFX.class, args);
    }
}
