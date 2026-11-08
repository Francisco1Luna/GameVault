package negocio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import dados.IRepositorioAlugueis;
import dados.IRepositorioVendas;
import dados.RepositorioAlugueisLista;
import dados.RepositorioVendasLista;
import model.Aluguel;
import model.Venda;

/**
 * REQ15 – Relatório de receita (locações + vendas) por mês.
 * Considera o valor registrado no momento da operação e agrupa pela data da locação/venda.
 */
public class RelatorioReceitaService {

  // Singleton: uma única instância em memória; use RelatorioReceitaService.getInstance().
  private static final RelatorioReceitaService INSTANCIA = new RelatorioReceitaService(
      RepositorioAlugueisLista.getInstance(), RepositorioVendasLista.getInstance());

  public static RelatorioReceitaService getInstance() {
    return INSTANCIA;
  }

  private final IRepositorioAlugueis repositorioAlugueis;
  private final IRepositorioVendas repositorioVendas;

  public RelatorioReceitaService(IRepositorioAlugueis repositorioAlugueis, IRepositorioVendas repositorioVendas) {
    if (repositorioAlugueis == null) {
      throw new IllegalArgumentException("O repositório de aluguéis é obrigatório.");
    }
    if (repositorioVendas == null) {
      throw new IllegalArgumentException("O repositório de vendas é obrigatório.");
    }
    this.repositorioAlugueis = repositorioAlugueis;
    this.repositorioVendas = repositorioVendas;
  }

  /**
   * Calcula a receita registrada por mês usando valores decimais exatos.
   * A locação é contabilizada no mês de início e as vendas no mês em que foram registradas.
   * O domínio não possui estado de pagamento/cancelamento; por isso, cada operação registrada
   * conta, independentemente de a locação ainda estar ativa ou já ter sido devolvida.
   * Registros sem data ou valor válido causam erro explícito para evitar totais silenciosamente incorretos.
   *
   * @return mapa ordenado e imutável por mês
   */
  public NavigableMap<YearMonth, ResumoMensal> resumoReceitaPorMes() {
    Map<YearMonth, BigDecimal[]> acumulados = new TreeMap<>();

    for (Aluguel aluguel : repositorioAlugueis.listar()) {
      if (aluguel == null) {
        throw new IllegalStateException("O repositório contém um aluguel nulo; não foi possível calcular a receita.");
      }
      YearMonth mes = mesObrigatorio(aluguel.getDataInicio(), "aluguel", aluguel.getId());
      BigDecimal valor = valorObrigatorio(aluguel.getValorLocacao(), "aluguel", aluguel.getId());
      BigDecimal[] valores = acumulados.computeIfAbsent(mes, chave -> novosAcumuladores());
      valores[0] = valores[0].add(valor);
    }

    for (Venda venda : repositorioVendas.listar()) {
      if (venda == null) {
        throw new IllegalStateException("O repositório contém uma venda nula; não foi possível calcular a receita.");
      }
      YearMonth mes = mesObrigatorio(venda.getDataVenda(), "venda", venda.getId());
      BigDecimal valor = valorObrigatorio(venda.getValorTotal(), "venda", venda.getId());
      BigDecimal[] valores = acumulados.computeIfAbsent(mes, chave -> novosAcumuladores());
      valores[1] = valores[1].add(valor);
    }

    NavigableMap<YearMonth, ResumoMensal> resultado = new TreeMap<>();
    acumulados.forEach((mes, valores) -> resultado.put(mes,
        new ResumoMensal(valores[0], valores[1], valores[0].add(valores[1]))));
    return Collections.unmodifiableNavigableMap(resultado);
  }

  /**
   * Compatibilidade com chamadas existentes. Para novos usos, prefira {@link #resumoReceitaPorMes()}.
   * Cada chamada cria um mapa e arrays novos, sem expor estado interno do serviço.
   */
  @Deprecated
  public Map<YearMonth, double[]> receitaPorMes() {
    Map<YearMonth, double[]> resultado = new TreeMap<>();
    resumoReceitaPorMes().forEach((mes, resumo) -> resultado.put(mes,
        new double[] { resumo.receitaLocacoes().doubleValue(), resumo.receitaVendas().doubleValue(),
            resumo.receitaTotal().doubleValue() }));
    return Collections.unmodifiableMap(resultado);
  }

  /** Gera uma tabela textual legível do relatório mensal de receita. */
  public String gerarRelatorioReceitaPorMes() {
    NavigableMap<YearMonth, ResumoMensal> receitas = resumoReceitaPorMes();
    StringBuilder sb = new StringBuilder();
    sb.append(String.format(Locale.ROOT, "%-10s | %18s | %18s | %18s%n", "Mês", "Receita Locações",
        "Receita Vendas", "Receita Total"));
    sb.append("-----------|--------------------|--------------------|--------------------\n");

    BigDecimal totalLocacoes = BigDecimal.ZERO;
    BigDecimal totalVendas = BigDecimal.ZERO;
    BigDecimal totalGeral = BigDecimal.ZERO;

    for (Map.Entry<YearMonth, ResumoMensal> entrada : receitas.entrySet()) {
      YearMonth mes = entrada.getKey();
      ResumoMensal resumo = entrada.getValue();
      sb.append(String.format(Locale.ROOT, "%-10s | %18.2f | %18.2f | %18.2f%n",
          mes.toString(), resumo.receitaLocacoes(), resumo.receitaVendas(), resumo.receitaTotal()));
      totalLocacoes = totalLocacoes.add(resumo.receitaLocacoes());
      totalVendas = totalVendas.add(resumo.receitaVendas());
      totalGeral = totalGeral.add(resumo.receitaTotal());
    }

    sb.append("-----------|--------------------|--------------------|--------------------\n");
    sb.append(String.format(Locale.ROOT, "%-10s | %18.2f | %18.2f | %18.2f%n", "TOTAL",
        totalLocacoes, totalVendas, totalGeral));

    return sb.toString();
  }

  private static BigDecimal[] novosAcumuladores() {
    return new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO };
  }

  private static YearMonth mesObrigatorio(java.time.LocalDate data, String tipo, Long id) {
    if (data == null) {
      throw new IllegalStateException("O " + tipo + " " + id + " não possui data e não pode entrar no relatório.");
    }
    return YearMonth.from(data);
  }

  private static BigDecimal valorObrigatorio(Double valor, String tipo, Long id) {
    if (valor == null || !Double.isFinite(valor) || valor < 0) {
      throw new IllegalStateException("O " + tipo + " " + id
          + " não possui um valor financeiro válido; não foi possível calcular a receita.");
    }
    return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
  }

  /** Imutável e tipado para evitar o uso de arrays mutáveis e valores double em dinheiro. */
  public record ResumoMensal(BigDecimal receitaLocacoes, BigDecimal receitaVendas, BigDecimal receitaTotal) {
    public ResumoMensal {
      if (receitaLocacoes == null || receitaVendas == null || receitaTotal == null) {
        throw new IllegalArgumentException("Os valores do resumo mensal são obrigatórios.");
      }
      receitaLocacoes = receitaLocacoes.setScale(2, RoundingMode.HALF_UP);
      receitaVendas = receitaVendas.setScale(2, RoundingMode.HALF_UP);
      receitaTotal = receitaTotal.setScale(2, RoundingMode.HALF_UP);
    }
  }
}
