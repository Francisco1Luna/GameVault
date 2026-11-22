package negocio;

import java.util.function.Supplier;

/**
 * Quem está logado agora. O AuthService abre e fecha a sessão (login/logout); os services
 * consultam aqui para recusar operações de administrador feitas por quem não é administrador,
 * em vez de confiar só nos botões escondidos da interface.
 *
 * É uma classe "folha" (não depende de nenhum service), por isso qualquer service pode usá-la
 * sem criar ciclo na inicialização dos Singletons. Guarda só o id e se o usuário é
 * administrador; o objeto Cliente em si continua sendo lido pelo AuthService.
 */
public final class Sessao {

    private static Long idUsuario;
    private static boolean administrador;

    // "modo sistema": cargas internas (dados de exemplo, administrador padrão) rodam sem login
    private static final ThreadLocal<Integer> MODO_SISTEMA = ThreadLocal.withInitial(() -> 0);

    private Sessao() { }

    static synchronized void entrar(Long id, boolean ehAdministrador) {
        idUsuario = id;
        administrador = ehAdministrador;
    }

    static synchronized void sair() {
        idUsuario = null;
        administrador = false;
    }

    public static synchronized Long idUsuarioAtual() {
        return idUsuario;
    }

    public static synchronized boolean ehAdministrador() {
        return administrador;
    }

    /**
     * Garante que a operação está sendo feita por um administrador logado (ou pelo próprio
     * sistema, dentro de {@link #executarComoSistema}). Caso contrário lança IllegalStateException.
     */
    public static void exigirAdministrador() {
        if (MODO_SISTEMA.get() > 0 || ehAdministrador()) {
            return;
        }
        throw new IllegalStateException("Operação restrita ao administrador.");
    }

    /** Executa uma carga interna sem exigir login. Vale só para a thread atual. */
    public static void executarComoSistema(Runnable acao) {
        executarComoSistema(() -> {
            acao.run();
            return null;
        });
    }

    public static <T> T executarComoSistema(Supplier<T> acao) {
        MODO_SISTEMA.set(MODO_SISTEMA.get() + 1);
        try {
            return acao.get();
        } finally {
            MODO_SISTEMA.set(MODO_SISTEMA.get() - 1);
        }
    }
}
