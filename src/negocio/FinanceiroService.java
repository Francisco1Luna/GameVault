package negocio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import model.Aluguel;
import model.Cliente;
import model.ClienteAssinante;
import model.Jogo;
import model.Penalidade;

/**
 * REQ12 - calcular multa por atraso na devolução, proporcional aos dias de atraso.
 * REQ13 - consultar pendências financeiras de um cliente (multas não pagas).
 * REQ11 - preço de venda com desconto automático para assinantes.
 */
public class FinanceiroService {

  //Valor cobrado por dia de atraso quando nenhuma taxa é informada.
  public static final double MULTA_DIARIA_PADRAO = 5.00;

  private final double multaDiaria;

  public FinanceiroService() {
    this(MULTA_DIARIA_PADRAO);
  }

  public FinanceiroService(double multaDiaria) {
    if (multaDiaria < 0) {
      throw new IllegalArgumentException("A multa diária não pode ser negativa.");
    }
    this.multaDiaria = multaDiaria;
  }

  // Valor da locação

  /**
   * Valor da locação = dias entre a retirada e o prazo de devolução (mínimo 1) x diária do jogo,
   * arredondado para centavos. O resultado é gravado no Aluguel e não muda se o preço mudar depois.
   */
  public double calcularValorLocacao(Jogo jogo, LocalDate dataInicio, LocalDate prazoDevolucao) {
    if (jogo == null) {
      throw new IllegalArgumentException("O jogo é obrigatório.");
    }
    if (jogo.getPrecoLocacaoDiaria() == null || jogo.getPrecoLocacaoDiaria() <= 0) {
      throw new IllegalArgumentException("O jogo não possui preço de locação diário válido.");
    }
    if (dataInicio == null || prazoDevolucao == null) {
      throw new IllegalArgumentException("A data de retirada e o prazo de devolução são obrigatórios.");
    }
    long dias = Math.max(1, ChronoUnit.DAYS.between(dataInicio, prazoDevolucao));
    double valor = dias * jogo.getPrecoLocacaoDiaria();
    return Math.round(valor * 100.0) / 100.0;
  }

  // REQ11

  /**
   * Preço unitário de venda: o preço do jogo, com o desconto do plano se o cliente for assinante.
   * O percentual do assinante é lido como porcentagem (10 = 10%) e deve estar entre 0 e 100.
   * Arredondado para centavos.
   */
  public double calcularPrecoVenda(Jogo jogo, Cliente cliente) {
    if (jogo == null) {
      throw new IllegalArgumentException("O jogo é obrigatório.");
    }
    exigirCliente(cliente);
    if (jogo.getPreco() == null || jogo.getPreco() <= 0) {
      throw new IllegalArgumentException("O jogo não possui preço de venda válido: " + jogo.getTitulo());
    }
    double preco = jogo.getPreco();
    if (cliente instanceof ClienteAssinante) {
      double percentual = ((ClienteAssinante) cliente).getPercentualDesconto();
      if (percentual < 0 || percentual > 100) {
        throw new IllegalArgumentException("Percentual de desconto inválido para o assinante: " + percentual);
      }
      preco = preco * (1 - percentual / 100.0);
    }
    return Math.round(preco * 100.0) / 100.0;
  }

  // REQ12

  /**
   * Dias de atraso: da devolução prevista até a devolução efetiva (ou até a data de referência,
   * se o aluguel ainda não foi devolvido). Nunca negativo.
   */
  public long calcularDiasDeAtraso(Aluguel aluguel, LocalDate dataReferencia) {
    exigirAluguelComPrazo(aluguel);
    LocalDate fim = aluguel.getDataDevolucaoEfetiva();
    if (fim == null) {
      if (dataReferencia == null) {
        throw new IllegalArgumentException("A data de referência é obrigatória para aluguel em aberto.");
      }
      fim = dataReferencia;
    }
    return Math.max(0, ChronoUnit.DAYS.between(aluguel.getDataDevolucaoPrevista(), fim));
  }

  /** Multa = dias de atraso x multa diária, arredondada para centavos. */
  public double calcularMulta(Aluguel aluguel, LocalDate dataReferencia) {
    double valor = calcularDiasDeAtraso(aluguel, dataReferencia) * multaDiaria;
    return Math.round(valor * 100.0) / 100.0;
  }

  /**
   * Gera a penalidade (não paga) de um aluguel já devolvido com atraso. Devolve vazio quando não
   * houve atraso e impede gerar duas multas para o mesmo aluguel.
   */
  public synchronized Optional<Penalidade> registrarMulta(Aluguel aluguel) {
    exigirAluguelComPrazo(aluguel);
    if (aluguel.getDataDevolucaoEfetiva() == null) {
      throw new IllegalStateException("A multa só pode ser registrada depois da devolução.");
    }
    if (aluguel.getPenalidade() != null) {
      throw new IllegalStateException("Já existe multa registrada para este aluguel.");
    }
    double valor = calcularMulta(aluguel, null);
    if (valor <= 0) {
      return Optional.empty();
    }
    // a penalidade é 1:1 com o aluguel, então reaproveita o id dele
    Penalidade penalidade = new Penalidade(aluguel.getId(), aluguel, valor, aluguel.getDataDevolucaoEfetiva(), false, null);
    aluguel.setPenalidade(penalidade); // a penalidade fica no aluguel, não no cliente
    return Optional.of(penalidade);
  }

  //  REQ13

  /** Multas ainda não pagas do cliente: percorre os aluguéis dele e pega a penalidade de cada um. */
  public List<Penalidade> consultarPendencias(Cliente cliente) {
    exigirCliente(cliente);
    List<Penalidade> pendentes = new ArrayList<>();
    if (cliente.getAlugueis() != null) {
      for (Aluguel aluguel : cliente.getAlugueis()) {
        Penalidade p = aluguel == null ? null : aluguel.getPenalidade();
        if (p != null && !p.isPaga()) {
          pendentes.add(p);
        }
      }
    }
    return pendentes;
  }

  /** Soma das multas não pagas do cliente. */
  public double calcularTotalPendente(Cliente cliente) {
    double total = 0.0;
    for (Penalidade p : consultarPendencias(cliente)) {
      total += p.getValor() == null ? 0.0 : p.getValor();
    }
    return Math.round(total * 100.0) / 100.0;
  }

  public boolean possuiPendencias(Cliente cliente) {
    return !consultarPendencias(cliente).isEmpty();
  }

  //apoio

  private static void exigirCliente(Cliente cliente) {
    if (cliente == null) {
      throw new IllegalArgumentException("O cliente é obrigatório.");
    }
  }

  private static void exigirAluguelComPrazo(Aluguel aluguel) {
    if (aluguel == null) {
      throw new IllegalArgumentException("O aluguel é obrigatório.");
    }
    if (aluguel.getDataDevolucaoPrevista() == null) {
      throw new IllegalArgumentException("O aluguel não possui data de devolução prevista.");
    }
  }
}
