package gui;

import dados.IRepositorioAlugueis;
import dados.IRepositorioJogos;
import dados.IRepositorioVendas;
import dados.RepositorioAlugueisLista;
import dados.RepositorioJogosLista;
import dados.RepositorioVendasLista;
import model.Aluguel;
import model.ClientePadrao;
import model.ItemVenda;
import model.Jogo;
import model.Plataforma;
import model.Venda;
import model.enums.ClassificacaoIndicativa;
import model.enums.StatusAluguel;
import negocio.RelatorioCatalogoApiService;
import negocio.RelatorioMaisLocadosService;
import negocio.RelatorioReceitaService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Principal {

  public static void main(String[] args) {
    System.out.println("==================================================");
    System.out.println("       GAMEVAULT - DEMONSTRAÇÃO DE RELATÓRIOS     ");
    System.out.println("==================================================\n");

    // 1. Obter Instâncias dos Repositórios (Singletons)
    IRepositorioJogos repoJogos = RepositorioJogosLista.getInstance();
    IRepositorioAlugueis repoAlugueis = RepositorioAlugueisLista.getInstance();
    IRepositorioVendas repoVendas = RepositorioVendasLista.getInstance();

    // 2. Criar Plataformas
    Plataforma pc = new Plataforma(1L, "PC", "Microsoft");
    Plataforma ps5 = new Plataforma(2L, "PlayStation 5", "Sony");

    // 3. Cadastrar Jogos (com os novos atributos preco e precoLocacaoDiaria)
    Jogo ow2 = new Jogo(null, "Overwatch 2", ClassificacaoIndicativa.DOZE_ANOS,
        "Blizzard Entertainment", "Shooter", "Blizzard", 29.90, 5.00,
        new ArrayList<>(Arrays.asList(pc, ps5)), new HashMap<>(),
        "https://www.freetogame.com/g/540/thumbnail.jpg",
        "https://www.freetogame.com/open/overwatch-2");

    Jogo valorant = new Jogo(null, "Valorant", ClassificacaoIndicativa.DEZESSEIS_ANOS,
        "Riot Games", "Shooter", "Riot Games", 24.90, 4.00,
        new ArrayList<>(Arrays.asList(pc)), new HashMap<>(),
        "https://www.freetogame.com/g/452/thumbnail.jpg",
        "https://www.freetogame.com/open/valorant");

    Jogo lostArk = new Jogo(null, "Lost Ark", ClassificacaoIndicativa.QUATORZE_ANOS,
        "Smilegate RPG", "MMORPG", "Amazon Games", 19.90, 3.50,
        new ArrayList<>(Arrays.asList(pc)), new HashMap<>(),
        "https://www.freetogame.com/g/517/thumbnail.jpg",
        "https://www.freetogame.com/open/lost-ark");

    Jogo fallGuys = new Jogo(null, "Fall Guys", ClassificacaoIndicativa.LIVRE,
        "Mediatonic", "Battle Royale", "Epic Games", 0.0, 2.00,
        new ArrayList<>(Arrays.asList(pc, ps5)), new HashMap<>(),
        "https://www.freetogame.com/g/523/thumbnail.jpg",
        "https://www.freetogame.com/open/fall-guys");

    Jogo jogoLocal = new Jogo(null, "Xadrez Clássico", ClassificacaoIndicativa.LIVRE,
        "Dev Local", "Tabuleiro", "Indie", 15.00, 2.50,
        new ArrayList<>(Arrays.asList(pc)), new HashMap<>(),
        null, null); // Manual (sem urlReferencia da API)

    repoJogos.salvar(ow2);
    repoJogos.salvar(valorant);
    repoJogos.salvar(lostArk);
    repoJogos.salvar(fallGuys);
    repoJogos.salvar(jogoLocal);

    // 4. Cadastrar Cliente
    ClientePadrao cliente = new ClientePadrao(1L, "Matheus Vasconcelos", "111.222.333-44",
        LocalDate.of(2000, 5, 10), "matheus@ufrpe.br", "81999998888");

    // 5. Cadastrar Aluguéis simulados
    Aluguel a1 = new Aluguel(null, cliente, ow2, pc,
        LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 12), LocalDate.of(2025, 1, 11), StatusAluguel.DEVOLVIDO);
    a1.setValorLocacao(35.00);
    repoAlugueis.salvar(a1);

    Aluguel a2 = new Aluguel(null, cliente, ow2, ps5,
        LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 22), LocalDate.of(2025, 1, 20), StatusAluguel.DEVOLVIDO);
    a2.setValorLocacao(35.00);
    repoAlugueis.salvar(a2);

    Aluguel a3 = new Aluguel(null, cliente, lostArk, pc,
        LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 9), LocalDate.of(2025, 2, 8), StatusAluguel.DEVOLVIDO);
    a3.setValorLocacao(24.50);
    repoAlugueis.salvar(a3);

    Aluguel a4 = new Aluguel(null, cliente, valorant, pc,
        LocalDate.of(2025, 2, 10), LocalDate.of(2025, 2, 17), LocalDate.of(2025, 2, 16), StatusAluguel.DEVOLVIDO);
    a4.setValorLocacao(28.00);
    repoAlugueis.salvar(a4);

    Aluguel a5 = new Aluguel(null, cliente, valorant, pc,
        LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 8), null, StatusAluguel.ATIVO);
    a5.setValorLocacao(28.00);
    repoAlugueis.salvar(a5);

    Aluguel a6 = new Aluguel(null, cliente, valorant, pc,
        LocalDate.of(2025, 3, 15), LocalDate.of(2025, 3, 22), null, StatusAluguel.ATIVO);
    a6.setValorLocacao(28.00);
    repoAlugueis.salvar(a6);

    // 6. Cadastrar Vendas simuladas
    ItemVenda item1 = new ItemVenda(ow2, pc, 1, 29.90);
    repoVendas.salvar(new Venda(null, cliente, LocalDate.of(2025, 1, 18), Arrays.asList(item1), 29.90));

    ItemVenda item2 = new ItemVenda(lostArk, pc, 2, 19.90);
    repoVendas.salvar(new Venda(null, cliente, LocalDate.of(2025, 2, 20), Arrays.asList(item2), 39.80));

    // =========================================================================
    // SERVIÇO 1: RelatorioMaisLocadosService (REQ14)
    // =========================================================================
    System.out.println(">>> REQ14: RelatorioMaisLocadosService (Jan a Mar/2025):");
    RelatorioMaisLocadosService maisLocadosService = RelatorioMaisLocadosService.getInstance();
    String csv = maisLocadosService.gerarCsvJogosMaisLocados(
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
    System.out.println(csv);

    // =========================================================================
    // SERVIÇO 2: RelatorioReceitaService (REQ15)
    // =========================================================================
    System.out.println(">>> REQ15: RelatorioReceitaService (Receita Mensal):");
    RelatorioReceitaService receitaService = RelatorioReceitaService.getInstance();
    System.out.println(receitaService.gerarRelatorioReceitaPorMes());

    // =========================================================================
    // SERVIÇO 3: RelatorioCatalogoApiService (REQ16)
    // =========================================================================
    System.out.println(">>> REQ16: RelatorioCatalogoApiService (Conferência de Todos os Jogos por Gênero):");
    RelatorioCatalogoApiService catalogoApiService = RelatorioCatalogoApiService.getInstance();

    // 1) Relatório completo impresso com todos os jogos da API agrupados por gênero:
    System.out.println(catalogoApiService.gerarRelatorioConferenciaCatalogo());

    // 2) Acesso ao mapa bruto agrupado por gênero (Map<String, List<Jogo>>):
    Map<String, List<Jogo>> jogosPorGenero = catalogoApiService.listarTodosImportadosAgrupadosPorGenero();
    System.out.println("Resumo do catálogo agrupado (chaves): " + jogosPorGenero.keySet());
    for (Map.Entry<String, List<Jogo>> entry : jogosPorGenero.entrySet()) {
      System.out.println("  • Gênero: " + entry.getKey() + " -> " + entry.getValue().size() + " jogo(s)");
    }
  }
}
