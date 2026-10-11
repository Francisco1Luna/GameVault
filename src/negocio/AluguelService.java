package negocio;

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

  private final EstoqueService estoqueService;
  private final IRepositorioAlugueis repositorioAlugueis;
  private final ValidadorMestre validadorMestre;

  public AluguelService(EstoqueService estoqueService, IRepositorioAlugueis repositorioAlugueis) {
    this(estoqueService, repositorioAlugueis, new ValidadorMestre());
  }

  public AluguelService(EstoqueService estoqueService, IRepositorioAlugueis repositorioAlugueis,
      ValidadorMestre validadorMestre) {
    if (estoqueService == null) {
      throw new IllegalArgumentException("O serviço de estoque é obrigatório.");
    }
    if (repositorioAlugueis == null) {
      throw new IllegalArgumentException("O repositório de aluguéis é obrigatório.");
    }
    if (validadorMestre == null) {
      throw new IllegalArgumentException("O validador mestre é obrigatório.");
    }
    this.estoqueService = estoqueService;
    this.repositorioAlugueis = repositorioAlugueis;
    this.validadorMestre = validadorMestre;
  }

  public synchronized Aluguel realizarLocacao(Cliente cliente, Jogo jogo, Plataforma plataforma,
      LocalDate dataInicio, LocalDate dataDevolucaoPrevista) throws ValidacaoException {
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

    estoqueService.removerExemplares(jogo, plataforma, 1);
    Aluguel aluguel = new Aluguel(null, cliente, jogo, plataforma, dataInicio,
        dataDevolucaoPrevista, null, StatusAluguel.ATIVO);
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