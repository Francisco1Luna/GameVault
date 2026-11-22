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

  // Singleton: uma única instância em memória; use FinanceiroService.getInstance().
  private static final FinanceiroService INSTANCIA = new FinanceiroService(
    MULTA_DIARIA_PADRAO);

  public static FinanceiroService getInstance() {
    return INSTANCIA;
  }

  private final double multaDiaria;

  private FinanceiroService(double multaDiaria) {
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
    if (aluguel.getPenalidade() != null && aluguel.getPenalidade().isPaga()) {
      throw new IllegalStateException("Já existe multa registrada para este aluguel.");
    }
    atualizarMulta(aluguel, null); // fecha o valor da multa que já corria durante o atraso
    return Optional.ofNullable(aluguel.getPenalidade());
  }

  /**
   * Mantém a multa do aluguel em dia: assim que passa do prazo ela já existe (pendente) e cresce a
   * cada dia de atraso até a devolução. Multa paga não muda mais.
   */
  public synchronized void atualizarMulta(Aluguel aluguel, LocalDate hoje) {
    if (aluguel == null || aluguel.getDataDevolucaoPrevista() == null) {
      return;
    }
    Penalidade penalidade = aluguel.getPenalidade();
    if (penalidade != null && penalidade.isPaga()) {
      return;
    }
    double valor = calcularMulta(aluguel, hoje);
    if (valor <= 0) {
      aluguel.setPenalidade(null); // sem atraso (ex.: o relógio do sistema foi reiniciado)
    } else if (penalidade == null) {
      // a penalidade é 1:1 com o aluguel, então reaproveita o id dele; gerada no 1º dia de atraso
      aluguel.setPenalidade(new Penalidade(aluguel.getId(), aluguel, valor,
          aluguel.getDataDevolucaoPrevista().plusDays(1), false, null));
    } else {
      penalidade.setValor(valor);
    }
  }

  //  REQ13

  /** Todas as multas do cliente (pagas e pendentes), com as de aluguéis em atraso já atualizadas. */
  public List<Penalidade> listarMultas(Cliente cliente) {
    exigirCliente(cliente);
    List<Penalidade> multas = new ArrayList<>();
    if (cliente.getAlugueis() != null) {
      LocalDate hoje = Relogio.hoje();
      for (Aluguel aluguel : cliente.getAlugueis()) {
        atualizarMulta(aluguel, hoje);
        if (aluguel != null && aluguel.getPenalidade() != null) {
          multas.add(aluguel.getPenalidade());
        }
      }
    }
    return multas;
  }

  /** Multas ainda não pagas do cliente, incluindo as que ainda correm em aluguéis atrasados. */
  public List<Penalidade> consultarPendencias(Cliente cliente) {
    List<Penalidade> pendentes = new ArrayList<>();
    for (Penalidade p : listarMultas(cliente)) {
      if (!p.isPaga()) {
        pendentes.add(p);
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

  /** Registra o pagamento de uma multa (só administrador). Não deixa pagar duas vezes a mesma multa. */
  public synchronized void pagarMulta(Penalidade penalidade, LocalDate dataPagamento) {
    Sessao.exigirAdministrador();
    if (penalidade == null) {
      throw new IllegalArgumentException("A multa é obrigatória.");
    }
    if (dataPagamento == null) {
      throw new IllegalArgumentException("A data de pagamento é obrigatória.");
    }
    if (penalidade.isPaga()) {
      throw new IllegalStateException("Esta multa já foi paga.");
    }
    if (penalidade.getAluguelOrigem() != null && penalidade.getAluguelOrigem().getDataDevolucaoEfetiva() == null) {
      throw new IllegalStateException("O jogo ainda não foi devolvido: a multa continua crescendo a cada dia de atraso. "
          + "Registre a devolução antes de quitar.");
    }
    penalidade.setPaga(true);
    penalidade.setDataPagamento(dataPagamento);
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
