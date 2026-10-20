package test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import dados.RepositorioAlugueisLista;
import dados.RepositorioClientesLista;
import dados.RepositorioJogosLista;
import dados.RepositorioVendasLista;
import dados.RepositoriosPlataformas;
import model.Aluguel;
import model.ClienteAssinante;
import model.ClientePadrao;
import model.ItemVenda;
import model.Jogo;
import model.Plataforma;
import model.Venda;
import model.enums.ClassificacaoIndicativa;
import model.enums.StatusAluguel;
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
 * Executa um fluxo de demonstração das funcionalidades implementadas no GameVault.
 *
 * <p>O arquivo não depende de JUnit para poder ser executado apenas com o JDK:
 * {@code javac -d out ...} seguido de {@code java -cp out test.TesteCompleto}.</p>
 * <p>Para executar o teste, utilize o seguinte comando no PowerShell:
 * {@code powershell -ExecutionPolicy Bypass -File test/TesteCompleto.ps1}.</p>
 * 
 * para executar:
 * powershell
 * New-Item -ItemType Directory -Force out | Out-Null
 * javac -d out (Get-ChildItem -Recurse src -Filter *.java)
 * java -cp out test.TesteCompleto 
 */

public class TesteCompleto {

  private static int verificacoes;
  private static int falhas;

  public static void main(String[] args) {
    imprimirCabecalho("GAMEVAULT - TESTE INTEGRADO");

    try {
      executarFluxoCompleto();
    } catch (RuntimeException | ValidacaoException e) {
      falhas++;
      System.out.println("FALHA INESPERADA: " + e.getMessage());
      e.printStackTrace(System.out);
    }

    System.out.println();
    System.out.println("Resultado: " + verificacoes + " verificações, " + falhas + " falha(s).");
    if (falhas > 0) {
      System.exit(1);
    }
    System.out.println("SUCESSO: todas as funcionalidades testadas funcionaram.");
  }

  private static void executarFluxoCompleto() throws ValidacaoException {
    RepositorioJogosLista repositorioJogos = new RepositorioJogosLista();
    RepositorioAlugueisLista repositorioAlugueis = new RepositorioAlugueisLista();
    RepositorioClientesLista repositorioClientes = new RepositorioClientesLista();
    RepositoriosPlataformas repositorioPlataformas = new RepositoriosPlataformas();
    RepositorioVendasLista repositorioVendas = new RepositorioVendasLista();

    PlataformaService plataformaService = new PlataformaService(repositorioPlataformas, repositorioJogos);
    JogoService jogoService = new JogoService(repositorioJogos, repositorioAlugueis);
    ClienteService clienteService = new ClienteService(repositorioClientes);
    EstoqueService estoqueService = new EstoqueService(repositorioJogos);
    FinanceiroService financeiroService = new FinanceiroService();
    AluguelService aluguelService = new AluguelService(
        estoqueService, repositorioAlugueis, financeiroService,
        new negocio.validacao.ValidadorMestre());
    DevolucaoService devolucaoService = new DevolucaoService(
        estoqueService, repositorioAlugueis, financeiroService);
    VendaService vendaService = new VendaService(
        estoqueService, repositorioVendas, financeiroService);

    imprimirCabecalho("1. CADASTROS E CONSULTAS");
    Plataforma pc = plataformaService.cadastrar(new Plataforma(null, "PC", "Microsoft"));
    Plataforma playstation = plataformaService.cadastrar(new Plataforma(null, "PlayStation", "Sony"));
    check(pc.getId() != null && playstation.getId() != null, "plataformas cadastradas com id");

    Jogo aventura = novoJogo(
        "Aventura Espacial", "Ação", ClassificacaoIndicativa.DEZ_ANOS,
        100.0, 10.0, pc, playstation);
    JogoService segundoServico = jogoService;
    jogoService.salvar(aventura);
    Jogo corrida = novoJogo(
        "Corrida Urbana", "Corrida", ClassificacaoIndicativa.LIVRE,
        80.0, 8.0, pc);
    jogoService.salvar(corrida);
    check(jogoService.buscarPorTitulo("espacial").size() == 1,
        "busca de jogo por título (sem diferenciar maiúsculas/minúsculas)");
    check(jogoService.listarPorGenero("CORRIDA").size() == 1,
        "listagem de jogos por gênero");
    check(jogoService.listarPorPlataforma(pc.getId()).size() == 2,
        "listagem de jogos por plataforma");
    check(jogoService.buscarPorId(aventura.getId()).isPresent(),
        "busca de jogo por id");
    check(segundoServico.listar().size() == 2, "listagem geral de jogos");

    ClientePadrao cliente = new ClientePadrao(
        null, "Cliente Comum", "111.111.111-11",
        LocalDate.now().minusYears(30), "comum@email.com", "81999999999");
    ClienteAssinante assinante = new ClienteAssinante(
        null, "Cliente Assinante", "222.222.222-22",
        LocalDate.now().minusYears(30), "assinante@email.com", "81888888888",
        10.0, LocalDate.now().minusMonths(2));
    clienteService.salvar(cliente);
    clienteService.salvar(assinante);
    check(clienteService.buscarPorCpf(cliente.getCpf()).isPresent(),
        "busca de cliente por CPF");
    check(clienteService.buscarPorId(assinante.getId()).isPresent(),
        "busca de cliente por id");
    check(cliente.getLimiteAluguelSimultaneo() == 1
        && assinante.getLimiteAluguelSimultaneo() == 3,
        "limites de locação dos tipos de cliente");

    imprimirCabecalho("2. ESTOQUE E DISPONIBILIDADE");
    estoqueService.definirEstoque(aventura, pc, 3);
    estoqueService.definirEstoque(aventura, playstation, 1);
    estoqueService.definirEstoque(corrida, pc, 4);
    check(estoqueService.consultarQuantidade(aventura, pc) == 3,
        "definição e consulta de estoque");
    check(estoqueService.estaDisponivel(aventura, playstation),
        "disponibilidade por plataforma");
    check(estoqueService.consultarDisponibilidade(aventura).size() == 2,
        "consulta de disponibilidade de todas as plataformas");
    estoqueService.adicionarExemplares(corrida, pc, 1);
    check(estoqueService.consultarQuantidade(corrida, pc) == 5,
        "adição de exemplares");
    estoqueService.removerExemplares(corrida, pc, 1);
    check(estoqueService.consultarQuantidade(corrida, pc) == 4,
        "remoção de exemplares");
    expectFailure("estoque insuficiente", IllegalStateException.class,
        () -> estoqueService.removerExemplares(corrida, pc, 99));

    imprimirCabecalho("3. LOCAÇÃO, DEVOLUÇÃO E REGRAS");
    LocalDate hoje = LocalDate.now();
    Aluguel locacaoComum = aluguelService.realizarLocacao(
        cliente, aventura, pc, hoje, hoje.plusDays(3));
    check(locacaoComum.getStatus() == StatusAluguel.ATIVO
        && estoqueService.consultarQuantidade(aventura, pc) == 2,
        "locação ativa reduz o estoque");
    check(locacaoComum.getValorLocacao() == 30.0,
        "valor da locação calculado pela diária e pelo prazo");
    expectFailure("limite de locações do cliente comum", ValidacaoException.class,
        () -> aluguelService.realizarLocacao(
            cliente, corrida, pc, hoje, hoje.plusDays(1)));
    devolucaoService.devolver(locacaoComum, hoje.plusDays(3));
    check(locacaoComum.getStatus() == StatusAluguel.DEVOLVIDO
        && estoqueService.consultarQuantidade(aventura, pc) == 3,
        "devolução finaliza a locação e repõe o estoque");
    expectFailure("devolução duplicada", ValidacaoException.class,
        () -> devolucaoService.devolver(locacaoComum, hoje.plusDays(4)));

    Aluguel locacaoAtrasada = aluguelService.realizarLocacao(
        assinante, aventura, playstation, hoje, hoje.plusDays(1));
    devolucaoService.devolver(locacaoAtrasada, hoje.plusDays(4));
    check(locacaoAtrasada.getPenalidade() != null
        && locacaoAtrasada.getPenalidade().getValor() == 15.0,
        "multa proporcional aos dias de atraso");
    check(financeiroService.consultarPendencias(assinante).size() == 1
        && financeiroService.calcularTotalPendente(assinante) == 15.0,
        "consulta de pendências financeiras");
    expectFailure("locação bloqueada por multa pendente", ValidacaoException.class,
        () -> aluguelService.realizarLocacao(
            assinante, corrida, pc, hoje, hoje.plusDays(1)));

    ClientePadrao menor = new ClientePadrao(
        null, "Cliente Menor", "333.333.333-33",
        LocalDate.now().minusYears(10), "menor@email.com", "81777777777");
    clienteService.salvar(menor);
    Jogo adulto = novoJogo(
        "Jogo Adulto", "Ação", ClassificacaoIndicativa.DEZOITO_ANOS,
        50.0, 5.0, pc);
    jogoService.salvar(adulto);
    estoqueService.definirEstoque(adulto, pc, 1);
    expectFailure("classificação indicativa incompatível", ValidacaoException.class,
        () -> aluguelService.realizarLocacao(
            menor, adulto, pc, hoje, hoje.plusDays(1)));

    imprimirCabecalho("4. VENDAS E DESCONTOS");
    double precoComum = financeiroService.calcularPrecoVenda(corrida, cliente);
    double precoAssinante = financeiroService.calcularPrecoVenda(corrida, assinante);
    check(precoComum == 80.0 && precoAssinante == 72.0,
        "desconto automático para assinante");
    Venda venda = vendaService.registrarVenda(
        assinante,
        Arrays.asList(new ItemVenda(corrida, pc, 2, 999.0)),
        hoje);
    check(venda.getValorTotal() == 144.0
        && venda.getItens().get(0).getPrecoUnitario() == 72.0,
        "venda registra itens e ignora preço informado pelo pedido");
    check(estoqueService.consultarQuantidade(corrida, pc) == 2,
        "venda baixa a quantidade correta do estoque");
    check(vendaService.buscarPorId(venda.getId()).isPresent()
        && vendaService.listarPorCliente(assinante).size() == 1
        && vendaService.listar().size() == 1,
        "consultas de vendas");
    expectFailure("venda sem itens", IllegalArgumentException.class,
        () -> vendaService.registrarVenda(assinante, new ArrayList<>(), hoje));

    imprimirCabecalho("5. RESTRIÇÕES DE REMOÇÃO");
    expectFailure("não remover plataforma usada por jogo", IllegalStateException.class,
        () -> plataformaService.remover(pc.getId()));
    aluguelService.realizarLocacao(
        cliente, adulto, pc, hoje, hoje.plusDays(2));
    expectFailure("não remover jogo com locação em andamento", IllegalStateException.class,
        () -> jogoService.remover(adulto.getId()));
    check(plataformaService.buscarPorId(playstation.getId()).isPresent(),
        "consulta de plataforma por id");
  }

  private static Jogo novoJogo(String titulo, String genero,
      ClassificacaoIndicativa classificacao, double preco, double diaria,
      Plataforma... plataformas) {
    return new Jogo(
        null, titulo, classificacao, "Estudio GameVault", genero,
        "Publicadora GameVault", preco, diaria,
        new ArrayList<>(Arrays.asList(plataformas)),
        new HashMap<>(), null, "https://example.com/" + titulo);
  }

  private static void imprimirCabecalho(String titulo) {
    System.out.println();
    System.out.println("=== " + titulo + " ===");
  }

  private static void check(boolean condicao, String descricao) {
    verificacoes++;
    if (!condicao) {
      falhas++;
      System.out.println("FALHA: " + descricao);
      return;
    }
    System.out.println("OK: " + descricao);
  }

  private static void expectFailure(String descricao, Class<? extends Throwable> tipo,
      AcaoDeTeste acao) {
    verificacoes++;
    try {
    acao.executar();
      falhas++;
      System.out.println("FALHA: " + descricao + " (nenhuma exceção foi lançada)");
    } catch (Throwable e) {
      if (!tipo.isInstance(e)) {
        falhas++;
        System.out.println("FALHA: " + descricao + " (recebido "
            + e.getClass().getSimpleName() + ")");
        return;
      }
      System.out.println("OK: " + descricao + " -> " + e.getMessage());
    }
  }

  @FunctionalInterface
  private interface AcaoDeTeste {
    void executar() throws Exception;
  }
}
