package negocio;

import dados.RepositorioAlugueisLista;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dados.IRepositorioAlugueis;
import model.Aluguel;
import model.Cliente;
import model.Jogo;
import model.Plataforma;
import model.enums.StatusAluguel;
import negocio.excecoes.ValidacaoException;
import negocio.validacao.ValidadorMestre;

/**
 * Serviço responsável pela realização de locações de jogos.
 */
public class AluguelService {

  // Singleton: uma única instância em memória; use AluguelService.getInstance().
  private static final AluguelService INSTANCIA = new AluguelService(
    EstoqueService.getInstance(), RepositorioAlugueisLista.getInstance(),
    FinanceiroService.getInstance(), new ValidadorMestre(), AuthService.getInstance());

  public static AluguelService getInstance() {
    return INSTANCIA;
  }

  private final EstoqueService estoqueService;
  private final IRepositorioAlugueis repositorioAlugueis;
  private final FinanceiroService financeiroService;
  private final ValidadorMestre validadorMestre;
  private final AuthService authService;

  private AluguelService(EstoqueService estoqueService, IRepositorioAlugueis repositorioAlugueis,
      FinanceiroService financeiroService, ValidadorMestre validadorMestre, AuthService authService) {
    if (estoqueService == null) {
      throw new IllegalArgumentException("O serviço de estoque é obrigatório.");
    }
    if (repositorioAlugueis == null) {
      throw new IllegalArgumentException("O repositório de aluguéis é obrigatório.");
    }
    if (financeiroService == null) {
      throw new IllegalArgumentException("O serviço financeiro é obrigatório.");
    }
    if (validadorMestre == null) {
      throw new IllegalArgumentException("O validador mestre é obrigatório.");
    }
    this.estoqueService = estoqueService;
    this.repositorioAlugueis = repositorioAlugueis;
    this.financeiroService = financeiroService;
    if (authService == null) {
      throw new IllegalArgumentException("O serviço de autenticação é obrigatório.");
    }
    this.validadorMestre = validadorMestre;
    this.authService = authService;
  }

  /**
   * Realiza a locação para o usuário atual (quem fez login no AuthService): o tipo dele define
   * o limite de locações simultâneas. Sem login, a locação é recusada (IllegalStateException).
   */
  public synchronized Aluguel realizarLocacao(Jogo jogo, Plataforma plataforma,
      LocalDate dataInicio, LocalDate dataDevolucaoPrevista) throws ValidacaoException {
    Cliente cliente = authService.exigirUsuarioAtual();
    exigirDadosLocacao(cliente, jogo, plataforma, dataInicio, dataDevolucaoPrevista);

    List<Aluguel> alugueis = cliente.getAlugueis();
    if (alugueis == null) {
      alugueis = new ArrayList<>();
      cliente.setAlugueis(alugueis);
    }

    validadorMestre.validar(cliente, jogo, filtrarAtivos(alugueis));
    if (estoqueService.consultarQuantidade(jogo, plataforma) <= 0) {
      throw new ValidacaoException("Licença indisponível para a plataforma selecionada.");
    }

    // calcula antes de mexer no estoque: se falhar, nada foi alterado
    double valorLocacao = financeiroService.calcularValorLocacao(jogo, dataInicio, dataDevolucaoPrevista);

    estoqueService.removerExemplares(jogo, plataforma, 1);
    Aluguel aluguel = new Aluguel(null, cliente, jogo, plataforma, dataInicio,
        dataDevolucaoPrevista, null, StatusAluguel.ATIVO);
    aluguel.setValorLocacao(valorLocacao); // valor congelado no momento da locação
    try {
      repositorioAlugueis.salvar(aluguel); // o repositório gera o id
    } catch (RuntimeException e) {
      estoqueService.adicionarExemplares(jogo, plataforma, 1); // desfaz a retirada do estoque
      throw e;
    }
    alugueis.add(aluguel);
    return aluguel;
  }

  /** Locações ainda em andamento (ATIVO ou ATRASADO); o histórico completo fica no cliente. */
  private static List<Aluguel> filtrarAtivos(List<Aluguel> alugueis) {
    List<Aluguel> ativos = new ArrayList<>();
    for (Aluguel aluguel : alugueis) {
      if (aluguel.getStatus() != StatusAluguel.DEVOLVIDO) {
        ativos.add(aluguel);
      }
    }
    return ativos;
  }

  private static void exigirDadosLocacao(Cliente cliente, Jogo jogo, Plataforma plataforma,
      LocalDate dataInicio, LocalDate dataDevolucaoPrevista) {
    if (cliente == null || jogo == null || plataforma == null) {
      throw new IllegalArgumentException("Cliente, jogo e plataforma são obrigatórios.");
    }
    if (dataInicio == null || dataDevolucaoPrevista == null) {
      throw new IllegalArgumentException("As datas da locação são obrigatórias.");
    }
    if (dataDevolucaoPrevista.isBefore(dataInicio)) {
      throw new IllegalArgumentException("A devolução prevista não pode preceder o início.");
    }
  }
}