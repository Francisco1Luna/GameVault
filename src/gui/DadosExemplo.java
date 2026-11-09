package gui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.Jogo;
import model.Plataforma;
import model.enums.ClassificacaoIndicativa;
import negocio.EstoqueService;
import negocio.JogoService;
import negocio.PlataformaService;

/** Cria plataformas, jogos e clientes de demonstração ao abrir o app (tudo é em memória). */
public final class DadosExemplo {

    private DadosExemplo() { }

    public static void carregar() {
        PlataformaService plataformaService = Contexto.get().getPlataformaService();
        JogoService jogoService = Contexto.get().getJogoService();
        EstoqueService estoqueService = Contexto.get().getEstoqueService();

        // evita duplicar se o método for chamado duas vezes
        if (!plataformaService.listar().isEmpty()) {
            return;
        }

        Plataforma ps5 = plataformaService.cadastrar(new Plataforma(null, "PlayStation 5", "Sony"));
        Plataforma xbox = plataformaService.cadastrar(new Plataforma(null, "Xbox Series X", "Microsoft"));
        Plataforma switchP = plataformaService.cadastrar(new Plataforma(null, "Nintendo Switch", "Nintendo"));

        criarJogo(jogoService, estoqueService, "Super Aventura", "Aventura",
                ClassificacaoIndicativa.LIVRE, 199.90, 9.90, 5, ps5, xbox, switchP);
        criarJogo(jogoService, estoqueService, "Corrida Total", "Corrida",
                ClassificacaoIndicativa.DEZ_ANOS, 149.90, 7.50, 3, ps5, xbox);
        criarJogo(jogoService, estoqueService, "Guerra Sombria", "Ação",
                ClassificacaoIndicativa.DEZOITO_ANOS, 249.90, 12.00, 2, ps5);

        // clientes de teste (senha: teste123)
        Contexto.get().getAuthService().cadastrar("Henrique Pad.", "11111111111",
                LocalDate.of(2000, 3, 15), null, null, "teste123", false);
        Contexto.get().getAuthService().cadastrar("Caio Ass.", "22222222222",
                LocalDate.of(1995, 7, 20), null, null, "teste123", true);
    }

    private static void criarJogo(JogoService jogoService, EstoqueService estoqueService, String titulo,
                                  String genero, ClassificacaoIndicativa classificacao, double preco, double locacao,
                                  int estoque, Plataforma... plataformas) {
        List<Plataforma> lista = new ArrayList<>(List.of(plataformas));
        Jogo jogo = jogoService.salvar(new Jogo(null, titulo, classificacao, null, genero, null,
                preco, locacao, lista, new HashMap<>(), null, null));
        for (Plataforma p : lista) {
            estoqueService.definirEstoque(jogo, p, estoque);
        }
    }
}