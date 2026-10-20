package test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import dados.RepositorioAlugueisLista;
import dados.RepositorioClientesLista;
import dados.RepositorioJogosLista;
import dados.RepositorioVendasLista;
import dados.RepositoriosPlataformas;
import model.Aluguel;
import model.Cliente;
import model.ClienteAssinante;
import model.ClientePadrao;
import model.ItemVenda;
import model.Jogo;
import model.Plataforma;
import model.Venda;
import model.enums.ClassificacaoIndicativa;
import negocio.AluguelService;
import negocio.ClienteService;
import negocio.DevolucaoService;
import negocio.EstoqueService;
import negocio.FinanceiroService;
import negocio.JogoService;
import negocio.PlataformaService;
import negocio.VendaService;
import negocio.excecoes.ValidacaoException;

/**
 * Simula a utilização do GameVault por um cliente através do terminal.
 *
 * <p>O programa usa repositórios em memória e dados de demonstração. Para iniciar:
 * {@code java -cp out test.MainInterativa}.</p>
 */
public class MainInterativa {

  private final Scanner scanner = new Scanner(System.in);
  private final RepositorioAlugueisLista repositorioAlugueis = new RepositorioAlugueisLista();
  private final RepositorioClientesLista repositorioClientes = new RepositorioClientesLista();
  private final RepositorioJogosLista repositorioJogos = new RepositorioJogosLista();
  private final RepositoriosPlataformas repositorioPlataformas = new RepositoriosPlataformas();
  private final RepositorioVendasLista repositorioVendas = new RepositorioVendasLista();

  private final PlataformaService plataformaService;
  private final JogoService jogoService;
  private final ClienteService clienteService;
  private final EstoqueService estoqueService;
  private final FinanceiroService financeiroService;
  private final AluguelService aluguelService;
  private final DevolucaoService devolucaoService;
  private final VendaService vendaService;

  private Cliente clienteAtual;

  public MainInterativa() {
    plataformaService = new PlataformaService(repositorioPlataformas, repositorioJogos);
    jogoService = new JogoService(repositorioJogos, repositorioAlugueis);
    financeiroService = new FinanceiroService();
    clienteService = new ClienteService(repositorioClientes, financeiroService);
    estoqueService = new EstoqueService(repositorioJogos);
    aluguelService = new AluguelService(
        estoqueService, repositorioAlugueis, financeiroService,
        new negocio.validacao.ValidadorMestre());
    devolucaoService = new DevolucaoService(
        estoqueService, repositorioAlugueis, financeiroService);
    vendaService = new VendaService(
        estoqueService, repositorioVendas, financeiroService);
  }

  public static void main(String[] args) {
    MainInterativa programa = new MainInterativa();
    programa.carregarDadosDeDemonstracao();
    programa.selecionarCliente();
    programa.executarMenu();
  }

  private void carregarDadosDeDemonstracao() {
    Plataforma pc = plataformaService.cadastrar(new Plataforma(null, "PC", "Microsoft"));
    Plataforma playstation = plataformaService.cadastrar(
        new Plataforma(null, "PlayStation", "Sony"));
    Plataforma xbox = plataformaService.cadastrar(new Plataforma(null, "Xbox", "Microsoft"));

    cadastrarJogo("Aventura Espacial", "Ação", ClassificacaoIndicativa.DEZ_ANOS,
        100.0, 10.0, 3, pc, playstation);
    cadastrarJogo("Corrida Urbana", "Corrida", ClassificacaoIndicativa.LIVRE,
        80.0, 8.0, 4, pc, xbox);
    cadastrarJogo("Mistério na Mansão", "Aventura", ClassificacaoIndicativa.DOZE_ANOS,
        60.0, 7.5, 2, playstation);

    clienteService.salvar(new ClientePadrao(
        null, "Cliente Comum", "111.111.111-11",
        LocalDate.now().minusYears(25), "comum@gamevault.test", "81000000000"));
    clienteService.salvar(new ClienteAssinante(
        null, "Cliente Assinante", "222.222.222-22",
        LocalDate.now().minusYears(30), "assinante@gamevault.test", "82000000000",
        10.0, LocalDate.now().minusMonths(3)));
    clienteService.salvar(new ClientePadrao(
        null, "Cliente Comum Junior", "111.111.111-12",
        LocalDate.now().minusYears(11), "comumjr@gamevault.test", "81000000001"));
  }

  private void cadastrarJogo(String titulo, String genero,
      ClassificacaoIndicativa classificacao, double preco, double diaria,
      int estoqueInicial, Plataforma... plataformas) {
    Jogo jogo = new Jogo(
        null, titulo, classificacao, "GameVault Studios", genero,
        "GameVault Publishing", preco, diaria,
        new ArrayList<>(Arrays.asList(plataformas)),
        new HashMap<>(), null, "https://example.com/" + titulo);
    jogoService.salvar(jogo);
    for (Plataforma plataforma : plataformas) {
      estoqueService.definirEstoque(jogo, plataforma, estoqueInicial);
    }
  }

  private void selecionarCliente() {
    System.out.println("==============================================");
    System.out.println("       GAMEVAULT - ATENDIMENTO NO TERMINAL");
    System.out.println("==============================================");
    System.out.println("Escolha o perfil para iniciar:");
    for (Cliente cliente : clienteService.listar()) {
      String tipo = cliente instanceof ClienteAssinante ? "Assinante" : "Comum";
      System.out.printf("%d - %s (%s)%n", cliente.getId(), cliente.getNome(), tipo);
    }

    while (clienteAtual == null) {
      long id = lerLong("ID do cliente: ");
      Optional<Cliente> cliente = clienteService.buscarPorId(id);
      if (cliente.isPresent()) {
        clienteAtual = cliente.get();
        System.out.println("Bem-vindo(a), " + clienteAtual.getNome() + "!");
      } else {
        System.out.println("Cliente não encontrado. Escolha um dos IDs exibidos.");
      }
    }
  }

  private void executarMenu() {
    boolean continuar = true;
    while (continuar) {
      mostrarMenu();
      int opcao = lerInteiro("Opção: ");
      try {
        switch (opcao) {
          case 1:
            listarJogos();
            break;
          case 2:
            consultarEstoque();
            break;
          case 3:
            listarMinhasLocacoes();
            break;
          case 4:
            realizarLocacao();
            break;
          case 5:
            devolverLocacao();
            break;
          case 6:
            realizarCompra();
            break;
          case 7:
            consultarPendencias();
            break;
          case 8:
            trocarCliente();
            break;
          case 0:
            continuar = false;
            System.out.println("Até logo, " + clienteAtual.getNome() + "!");
            break;
          default:
            System.out.println("Opção inválida.");
        }
      } catch (RuntimeException | ValidacaoException e) {
        System.out.println("Não foi possível concluir a operação: " + e.getMessage());
      }
    }
    scanner.close();
  }

  private void mostrarMenu() {
    System.out.println();
    System.out.println("----------------------------------------------");
    System.out.println("Cliente: " + clienteAtual.getNome()
        + (clienteAtual instanceof ClienteAssinante ? " [ASSINANTE]" : " [COMUM]"));
    System.out.println("1 - Ver catálogo de jogos");
    System.out.println("2 - Consultar estoque");
    System.out.println("3 - Ver minhas locações");
    System.out.println("4 - Alugar jogo");
    System.out.println("5 - Devolver jogo");
    System.out.println("6 - Comprar jogo");
    System.out.println("7 - Consultar pendências financeiras");
    System.out.println("8 - Trocar cliente");
    System.out.println("0 - Sair");
  }

  private void listarJogos() {
    System.out.println();
    System.out.println("CATÁLOGO");
    for (Jogo jogo : jogoService.listar()) {
      System.out.printf("%d - %s | gênero: %s | classificação: %s | venda: R$ %.2f | diária: R$ %.2f%n",
          jogo.getId(), jogo.getTitulo(), jogo.getGenero(),
          jogo.getClassificacaoIndicativa(), jogo.getPreco(), jogo.getPrecoLocacaoDiaria());
    }
  }

  private void consultarEstoque() {
    System.out.println();
    System.out.println("ESTOQUE DISPONÍVEL");
    for (Jogo jogo : jogoService.listar()) {
      System.out.println(jogo.getId() + " - " + jogo.getTitulo());
      for (Plataforma plataforma : jogo.getPlataformas()) {
        System.out.printf("   %s: %d unidade(s)%n", plataforma.getNome(),
            estoqueService.consultarQuantidade(jogo, plataforma));
      }
    }
  }

  private void listarMinhasLocacoes() {
    System.out.println();
    System.out.println("MINHAS LOCAÇÕES");
    List<Aluguel> alugueis = repositorioAlugueis.listarPorCliente(clienteAtual);
    if (alugueis.isEmpty()) {
      System.out.println("Nenhuma locação encontrada.");
      return;
    }
    for (Aluguel aluguel : alugueis) {
      System.out.printf("%d - %s / %s | início: %s | devolução prevista: %s | status: %s%n",
          aluguel.getId(), aluguel.getJogo().getTitulo(), aluguel.getPlataforma().getNome(),
          aluguel.getDataInicio(), aluguel.getDataDevolucaoPrevista(), aluguel.getStatus());
      if (aluguel.getPenalidade() != null && !aluguel.getPenalidade().isPaga()) {
        System.out.printf("   Multa pendente: R$ %.2f%n", aluguel.getPenalidade().getValor());
      }
    }
  }

  private void realizarLocacao() throws ValidacaoException {
    listarJogos();
    Jogo jogo = escolherJogo();
    Plataforma plataforma = escolherPlataforma(jogo);
    int dias = lerInteiro("Por quantos dias deseja alugar? ");
    if (dias < 1) {
      throw new IllegalArgumentException("A quantidade de dias deve ser positiva.");
    }
    LocalDate inicio = LocalDate.now();
    Aluguel aluguel = aluguelService.realizarLocacao(
        clienteAtual, jogo, plataforma, inicio, inicio.plusDays(dias));
    System.out.printf("Locação #%d realizada! Valor: R$ %.2f. Devolução prevista: %s%n",
        aluguel.getId(), aluguel.getValorLocacao(), aluguel.getDataDevolucaoPrevista());
  }

  private void devolverLocacao() throws ValidacaoException {
    listarMinhasLocacoes();
    long id = lerLong("ID da locação para devolver: ");
    Optional<Aluguel> aluguel = repositorioAlugueis.buscarPorId(id);
    if (!aluguel.isPresent()
        || !clienteAtual.getId().equals(aluguel.get().getCliente().getId())) {
      throw new IllegalArgumentException("Locação não encontrada para este cliente.");
    }
    String entrada = lerTexto("Data da devolução (AAAA-MM-DD; vazio = hoje): ");
    LocalDate data = entrada.isEmpty() ? LocalDate.now() : LocalDate.parse(entrada);
    devolucaoService.devolver(aluguel.get(), data);
    System.out.println("Devolução registrada com sucesso.");
    if (aluguel.get().getPenalidade() != null) {
      System.out.printf("Atenção: multa gerada de R$ %.2f.%n",
          aluguel.get().getPenalidade().getValor());
    }
  }

  private void realizarCompra() {
    listarJogos();
    Jogo jogo = escolherJogo();
    Plataforma plataforma = escolherPlataforma(jogo);
    int quantidade = lerInteiro("Quantidade: ");
    ItemVenda item = new ItemVenda(jogo, plataforma, quantidade, null);
    Venda venda = vendaService.registrarVenda(
        clienteAtual, Arrays.asList(item), LocalDate.now());
    System.out.printf("Compra #%d realizada! Total: R$ %.2f%n",
        venda.getId(), venda.getValorTotal());
  }

  private void consultarPendencias() {
    double total = financeiroService.calcularTotalPendente(clienteAtual);
    System.out.printf("Total de multas pendentes: R$ %.2f%n", total);
    if (total > 0) {
      financeiroService.consultarPendencias(clienteAtual).forEach(penalidade ->
          System.out.printf("- Locação #%d: R$ %.2f%n",
              penalidade.getAluguelOrigem().getId(), penalidade.getValor()));
    }
  }

  private void trocarCliente() {
    clienteAtual = null;
    selecionarCliente();
  }

  private Jogo escolherJogo() {
    long id = lerLong("ID do jogo: ");
    return jogoService.buscarPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado."));
  }

  private Plataforma escolherPlataforma(Jogo jogo) {
    System.out.println("Plataformas disponíveis para " + jogo.getTitulo() + ":");
    for (Plataforma plataforma : jogo.getPlataformas()) {
      System.out.printf("%d - %s (%d unidade(s))%n", plataforma.getId(),
          plataforma.getNome(), estoqueService.consultarQuantidade(jogo, plataforma));
    }
    long id = lerLong("ID da plataforma: ");
    return jogo.getPlataformas().stream()
        .filter(plataforma -> plataforma.getId().equals(id))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Plataforma não disponível para este jogo."));
  }

  private String lerTexto(String mensagem) {
    System.out.print(mensagem);
    return scanner.nextLine().trim();
  }

  private int lerInteiro(String mensagem) {
    while (true) {
      try {
        return Integer.parseInt(lerTexto(mensagem));
      } catch (NumberFormatException e) {
        System.out.println("Digite um número inteiro válido.");
      }
    }
  }

  private long lerLong(String mensagem) {
    while (true) {
      try {
        return Long.parseLong(lerTexto(mensagem));
      } catch (NumberFormatException e) {
        System.out.println("Digite um ID numérico válido.");
      }
    }
  }
}
