package gui;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

/** Máscaras de apresentação para campos usados nas telas JavaFX. */
public final class Formatadores {
    private Formatadores() { }

    public static void cpf(TextField campo) {
        aplicarMascara(campo, 11, Formatadores::formatarCpf);
    }

    public static void telefone(TextField campo) {
        aplicarMascara(campo, 11, Formatadores::formatarTelefone);
    }

    public static void decimal(TextField campo) {
        campo.setTextFormatter(new TextFormatter<String>(change -> {
            String novo = change.getControlNewText();
            return novo.matches("[0-9]*([.,][0-9]*)?") ? change : null;
        }));
    }

    public static void inteiro(TextField campo) {
        campo.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("[0-9]*") ? change : null));
    }

    public static String formatarCpf(String valor) {
        String digitos = somenteDigitos(valor, 11);
        StringBuilder texto = new StringBuilder(digitos);
        if (digitos.length() > 3) texto.insert(3, '.');
        if (digitos.length() > 6) texto.insert(7, '.');
        if (digitos.length() > 9) texto.insert(11, '-');
        return texto.toString();
    }

    public static String formatarTelefone(String valor) {
        String d = somenteDigitos(valor, 11);
        if (d.length() <= 2) return d.isEmpty() ? d : "(" + d;
        String prefixo = "(" + d.substring(0, 2) + ") ";
        if (d.length() <= 6) return prefixo + d.substring(2);
        int separador = d.length() == 11 ? 7 : 6;
        return prefixo + d.substring(2, separador) + "-" + d.substring(separador);
    }

    private static void aplicarMascara(TextField campo, int limite,
                                        java.util.function.Function<String, String> formatar) {
        UnaryOperator<TextFormatter.Change> filtro = change -> {
            if (change.getControlNewText().isEmpty()) return change;
            String digitos = somenteDigitos(change.getControlNewText(), limite);
            change.setText(formatar.apply(digitos));
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(change.getText().length());
            change.setAnchor(change.getText().length());
            return change;
        };
        campo.setTextFormatter(new TextFormatter<>(filtro));
    }

    private static String somenteDigitos(String valor, int limite) {
        if (valor == null) return "";
        StringBuilder digitos = new StringBuilder();
        for (int i = 0; i < valor.length() && digitos.length() < limite; i++) {
            if (Character.isDigit(valor.charAt(i))) digitos.append(valor.charAt(i));
        }
        return digitos.toString();
    }
}
