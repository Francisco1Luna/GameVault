package negocio;

import java.time.LocalDate;

/** Data do sistema. Permite avançar dias para testar atrasos e multas sem esperar. */
public final class Relogio {

    private static int diasAvancados = 0;

    private Relogio() { }

    public static synchronized LocalDate hoje() {
        return LocalDate.now().plusDays(diasAvancados);
    }

    public static synchronized void avancarDias(int dias) {
        if (dias <= 0) {
            throw new IllegalArgumentException("Informe uma quantidade de dias maior que zero.");
        }
        diasAvancados += dias;
    }

    public static synchronized void reiniciar() {
        diasAvancados = 0;
    }
}