package negocio;

import dados.RepositorioAlugueisLista;
import java.time.LocalDate;

import dados.IRepositorioAlugueis;
import model.Aluguel;
import model.Penalidade;
import model.enums.StatusAluguel;
import negocio.excecoes.ValidacaoException;

/**
 * Serviço responsável pela devolução de jogos locados.
 */
public class DevolucaoService {

  // Singleton: uma única instância em memória; use DevolucaoService.getInstance().
  private static final DevolucaoService INSTANCIA = new DevolucaoService(
    EstoqueService.getInstance(), RepositorioAlugueisLista.getInstance(),
    FinanceiroService.getInstance());

  public static DevolucaoService getInstance() {
    return INSTANCIA;
  }

  private final EstoqueService estoqueService;
  private final IRepositorioAlugueis repositorioAlugueis;
  private final FinanceiroService financeiroService;

  private DevolucaoService(EstoqueService estoqueService, IRepositorioAlugueis repositorioAlugueis,
      FinanceiroService financeiroService) {
    if (estoqueService == null) {
      throw new IllegalArgumentException("O serviço de estoque é obrigatório.");
    }
    if (repositorioAlugueis == null) {
      throw new IllegalArgumentException("O repositório de aluguéis é obrigatório.");
    }
    if (financeiroService == null) {
      throw new IllegalArgumentException("O serviço financeiro é obrigatório.");
    }
    this.estoqueService = estoqueService;
    this.repositorioAlugueis = repositorioAlugueis;
    this.financeiroService = financeiroService;
  }

  /**
   * Registra a devolução: devolve o exemplar ao estoque, finaliza o aluguel e gera a multa se
   * houve atraso. É tudo ou nada: se qualquer passo falhar, o estoque e o aluguel voltam ao que eram.
   */
  public synchronized void devolver(Aluguel aluguel, LocalDate dataDevolucao)
      throws ValidacaoException {
    if (aluguel == null) {
      throw new IllegalArgumentException("O aluguel é obrigatório.");
    }
    if (dataDevolucao == null) {
      throw new IllegalArgumentException("A data de devolução é obrigatória.");
    }
    if (aluguel.getStatus() == StatusAluguel.DEVOLVIDO
        || aluguel.getDataDevolucaoEfetiva() != null) {
      throw new ValidacaoException("A locação já foi finalizada.");
    }
    if (aluguel.getId() == null || repositorioAlugueis.buscarPorId(aluguel.getId()).isEmpty()) {
      throw new IllegalArgumentException("O aluguel não está cadastrado.");
    }

    if (aluguel.getDataInicio() != null && dataDevolucao.isBefore(aluguel.getDataInicio())) {
      throw new ValidacaoException("A devolução não pode ser anterior ao início da locação.");
    }

    StatusAluguel statusAnterior = aluguel.getStatus();
    Penalidade penalidadeAnterior = aluguel.getPenalidade();
    boolean estoqueDevolvido = false;
    try {
      estoqueService.adicionarExemplares(aluguel.getJogo(), aluguel.getPlataforma(), 1);
      estoqueDevolvido = true;
      aluguel.setDataDevolucaoEfetiva(dataDevolucao);
      aluguel.setStatus(StatusAluguel.DEVOLVIDO);
      // sempre chamado: fecha o valor da multa que já corria, ou a remove se a devolução ficou no prazo
      financeiroService.registrarMulta(aluguel);
      repositorioAlugueis.atualizar(aluguel);
    } catch (RuntimeException e) {
      // desfaz tudo, para a locação continuar em aberto e poder ser devolvida de novo
      aluguel.setDataDevolucaoEfetiva(null);
      aluguel.setStatus(statusAnterior);
      aluguel.setPenalidade(penalidadeAnterior);
      if (estoqueDevolvido) {
        try {
          estoqueService.removerExemplares(aluguel.getJogo(), aluguel.getPlataforma(), 1);
        } catch (RuntimeException ignorada) {
          // o saldo recém-somado não some sozinho; se falhar não há mais o que desfazer
        }
      }
      throw e;
    }
  }
}
