package negocio;

/**
 * Regras de CPF: normalização (só dígitos) e validação dos dois dígitos verificadores.
 * Classe utilitária sem estado, usada por ClienteService e AuthService.
 */
public final class Cpf {

    /**
     * CPF reservado ao administrador de demonstração (00000000000). Ele não passa nos dígitos
     * verificadores e só é aceito em um cliente administrador (ver ClienteService).
     */
    public static final String RESERVADO_ADMINISTRADOR = "00000000000";

    private Cpf() { }

    /** Tira pontos, traços e espaços. Texto nulo vira "". */
    public static String normalizar(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    /**
     * O CPF (com ou sem máscara) tem 11 dígitos, não é uma sequência repetida (111.111.111-11)
     * e os dois dígitos verificadores conferem.
     */
    public static boolean ehValido(String cpf) {
        String d = normalizar(cpf);
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        return digito(d, 9) == d.charAt(9) - '0' && digito(d, 10) == d.charAt(10) - '0';
    }

    /** Dígito verificador calculado sobre os primeiros {@code quantidade} dígitos. */
    private static int digito(String d, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (d.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
