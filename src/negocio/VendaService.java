package negocio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.Aluguel;
import model.Cliente;
import model.Jogo;
import model.enums.Plataforma;
import model.enums.StatusAluguel;
import negocio.excecoes.ValidacaoException;
import negocio.validacao.ValidadorMestre;

/**
 * Serviço responsável pela realização de locações de jogos.
 */
public class VendaService {

  private final EstoqueService estoqueService;
  private final ValidadorMestre validadorMestre;
  private long proximoId = 1;

  public VendaService(EstoqueService estoqueService) {
    this(estoqueService, new ValidadorMestre());
  }

  public VendaService(EstoqueService estoqueService, ValidadorMestre validadorMestre) {
    if (estoqueService == null) {
      throw new IllegalArgumentException("O serviço de estoque é obrigatório.");
    }
    if (validadorMestre == null) {
      throw new IllegalArgumentException("O validador mestre é obrigatório.");
    }
    this.estoqueService = estoqueService;
    this.validadorMestre = validadorMestre;
  }

  public synchronized Aluguel realizarLocacao(Cliente cliente, Jogo jogo, Plataforma plataforma,
      LocalDate dataInicio, LocalDate dataDevolucaoPrevista) throws ValidacaoException {
    exigirDadosLocacao(cliente, jogo, plataforma, dataInicio, dataDevolucaoPrevista);

    List<Aluguel> alugueisAtivos = cliente.getAlugueisAtivos();
    if (alugueisAtivos == null) {
      alugueisAtivos = new ArrayList<>();
      cliente.setAlugueisAtivos(alugueisAtivos);
    }

    validadorMestre.validar(cliente, jogo, alugueisAtivos);
    if (estoqueService.consultarQuantidade(jogo, plataforma) <= 0) {
      throw new ValidacaoException("Licença indisponível para a plataforma selecionada.");
    }

    estoqueService.removerExemplares(jogo, plataforma, 1);
    Aluguel aluguel = new Aluguel(proximoId++, cliente, jogo, plataforma, dataInicio,
        dataDevolucaoPrevista, null, StatusAluguel.ATIVO);
    alugueisAtivos.add(aluguel);
    return aluguel;
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