package gui;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/** Restringe e formata entradas com padrão conhecido. */
public final class Formatadores {
    private Formatadores() { }

    public static void cpf(TextField campo) { aplicarMascara(campo, 11, Formatadores::formatarCpf); }
    public static void telefone(TextField campo) { aplicarMascara(campo, 11, Formatadores::formatarTelefone); }

    public static void decimal(TextField campo) {
        campo.setTextFormatter(new TextFormatter<String>(mudanca ->
                mudanca.getControlNewText().matches("[0-9]*([.,][0-9]*)?") ? mudanca : null));
    }

    public static void inteiro(TextField campo) {
        campo.setTextFormatter(new TextFormatter<String>(mudanca ->
                mudanca.getControlNewText().matches("[0-9]*") ? mudanca : null));
    }

    public static String formatarCpf(String valor) {
        String d = somenteDigitos(valor, 11);
        StringBuilder resultado = new StringBuilder(d);
        if (d.length() > 3) resultado.insert(3, '.');
        if (d.length() > 6) resultado.insert(7, '.');
        if (d.length() > 9) resultado.insert(11, '-');
        return resultado.toString();
    }

    public static String formatarTelefone(String valor) {
        String d = somenteDigitos(valor, 11);
        if (d.length() <= 2) return d.isEmpty() ? "" : "(" + d;
        String inicio = "(" + d.substring(0, 2) + ") ";
        if (d.length() <= 6) return inicio + d.substring(2);
        int separador = d.length() == 11 ? 7 : 6;
        return inicio + d.substring(2, separador) + "-" + d.substring(separador);
    }

    private static void aplicarMascara(TextField campo, int limite,
                                       java.util.function.Function<String, String> formatador) {
        campo.setTextFormatter(new TextFormatter<String>(mudanca -> {
            String texto = mudanca.getControlNewText();
            if (texto.isEmpty()) return mudanca;
            String formatado = formatador.apply(somenteDigitos(texto, limite));
            mudanca.setRange(0, mudanca.getControlText().length());
            mudanca.setText(formatado);
            mudanca.setCaretPosition(formatado.length());
            mudanca.setAnchor(formatado.length());
            return mudanca;
        }));
    }

    private static String somenteDigitos(String texto, int limite) {
        if (texto == null) return "";
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < texto.length() && resultado.length() < limite; i++) {
            if (Character.isDigit(texto.charAt(i))) resultado.append(texto.charAt(i));
        }
        return resultado.toString();
    }
}
