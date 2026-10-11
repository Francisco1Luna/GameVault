package negocio;

import java.time.LocalDate;

import dados.RepositorioPenalidadesLista;
import model.Aluguel;
import model.Cliente;
import model.enums.StatusAluguel;
import negocio.excecoes.ValidacaoException;

/**
 * Serviço responsável pela devolução de jogos locados.
 */
public class DevolucaoService {

  private final EstoqueService estoqueService;
  private final FinanceiroService financeiroService;

  public DevolucaoService(EstoqueService estoqueService) {
    this(estoqueService, new FinanceiroService(new RepositorioPenalidadesLista()));
  }

  public DevolucaoService(EstoqueService estoqueService, FinanceiroService financeiroService) {
    if (estoqueService == null) {
      throw new IllegalArgumentException("O serviço de estoque é obrigatório.");
    }
    if (financeiroService == null) {
      throw new IllegalArgumentException("O serviço financeiro é obrigatório.");
    }
    this.estoqueService = estoqueService;
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

    long diasDeAtraso = financeiroService.calcularDiasDeAtraso(aluguel, dataDevolucao);
    estoqueService.adicionarExemplares(aluguel.getJogo(), aluguel.getPlataforma(), 1);
    aluguel.setDataDevolucaoEfetiva(dataDevolucao);
    aluguel.setStatus(StatusAluguel.DEVOLVIDO);
    if (diasDeAtraso > 0) {
      financeiroService.registrarMulta(aluguel);
    }

    Cliente cliente = aluguel.getCliente();
    if (cliente != null && cliente.getAlugueisAtivos() != null) {
      cliente.getAlugueisAtivos().remove(aluguel);
    }
  }
}
