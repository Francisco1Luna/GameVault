package gui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comparadores para colunas que mostram texto formatado. Sem eles, clicar no cabeçalho ordena
 * como texto: "R$ 100,00" ficaria antes de "R$ 20,00" e "01/12/2026" antes de "02/01/2026".
 * Uso: coluna.setComparator(Ordenacao.DINHEIRO). Textos sem valor ("-", vazio) vão para o fim.
 */
public final class Ordenacao {

    private Ordenacao() { }

    private static final Pattern VALOR = Pattern.compile("-?[\\d.]+(,\\d+)?");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MM/yyyy");

    /** "R$ 1.234,56" (usa o primeiro valor: em "R$ 179,91 (de R$ 199,90)" vale 179,91). */
    public static final Comparator<String> DINHEIRO = por(Ordenacao::lerValor);

    /** Números inteiros como texto ("5", "12"). */
    public static final Comparator<String> NUMERO = por(Ordenacao::lerValor);

    /** Datas "dd/MM/yyyy". */
    public static final Comparator<String> DATA_BR = por(texto -> {
        try {
            return LocalDate.parse(texto.trim(), DATA);
        } catch (DateTimeParseException e) {
            return null;
        }
    });

    /** Meses "MM/yyyy". */
    public static final Comparator<String> MES_ANO = por(texto -> {
        try {
            return YearMonth.parse(texto.trim(), MES);
        } catch (DateTimeParseException e) {
            return null;
        }
    });

    private static BigDecimal lerValor(String texto) {
        Matcher m = VALOR.matcher(texto);
        if (!m.find()) {
            return null;
        }
        String numero = m.group().replace(".", "").replace(',', '.');
        try {
            return new BigDecimal(numero);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Converte o texto com a função e compara; o que não converte (null) fica por último. */
    private static <T extends Comparable<? super T>> Comparator<String> por(Function<String, T> conversor) {
        Function<String, T> seguro = texto -> texto == null ? null : conversor.apply(texto);
        return Comparator.comparing(seguro, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
