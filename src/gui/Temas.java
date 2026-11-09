package gui;

import javafx.scene.Scene;

/** Aplica e alterna o tema (CSS) da janela. O CSS base vale para os dois temas. */
public final class Temas {

    private static final String BASE = "/gui/css/base.css";
    private static final String CLARO = "/gui/css/tema-claro.css";
    private static final String ESCURO = "/gui/css/tema-escuro.css";

    private static boolean escuro = false;

    private Temas() { }

    public static void aplicar(Scene cena) {
        cena.getStylesheets().setAll(url(BASE), url(escuro ? ESCURO : CLARO));
    }

    public static void alternar(Scene cena) {
        escuro = !escuro;
        aplicar(cena);
    }

    private static String url(String caminho) {
        return Temas.class.getResource(caminho).toExternalForm();
    }
}