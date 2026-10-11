package negocio;

import java.time.LocalDate;

import dados.IRepositorioAlugueis;
import model.Aluguel;
import model.enums.StatusAluguel;
import negocio.excecoes.ValidacaoException;

/**
 * Serviço responsável pela devolução de jogos locados.
 */
public class DevolucaoService {

  private final EstoqueService estoqueService;
  private final IRepositorioAlugueis repositorioAlugueis;
  private final FinanceiroService financeiroService;

  public DevolucaoService(EstoqueService estoqueService, IRepositorioAlugueis repositorioAlugueis) {
    this(estoqueService, repositorioAlugueis, new FinanceiroService());
  }

  public DevolucaoService(EstoqueService estoqueService, IRepositorioAlugueis repositorioAlugueis,
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

    long diasDeAtraso = financeiroService.calcularDiasDeAtraso(aluguel, dataDevolucao);
    estoqueService.adicionarExemplares(aluguel.getJogo(), aluguel.getPlataforma(), 1);
    aluguel.setDataDevolucaoEfetiva(dataDevolucao);
    aluguel.setStatus(StatusAluguel.DEVOLVIDO);
    if (diasDeAtraso > 0) {
      financeiroService.registrarMulta(aluguel);
    }
    repositorioAlugueis.atualizar(aluguel);
  }
}
