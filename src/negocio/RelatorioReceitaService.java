package negocio;

import java.time.YearMonth;
import java.util.Map;
import java.util.TreeMap;

import dados.IRepositorioAlugueis;
import dados.IRepositorioVendas;
import dados.RepositorioAlugueisLista;
import dados.RepositorioVendasLista;
import model.Aluguel;
import model.Venda;

/**
 * REQ15 – Relatório de receita (locações + vendas) por mês.
 * Depende dos repositórios de aluguéis e vendas.
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
   * Calcula a receita total por mês, somando receita de locações e de vendas.
   * Retorna um mapa ordenado por mês (YearMonth), onde cada entrada contém um array
   * de 3 valores: [receitaLocacoes, receitaVendas, receitaTotal].
   *
   * @return mapa ordenado de YearMonth para double[3] {receitaLocacoes, receitaVendas, receitaTotal}
   */
  public Map<YearMonth, double[]> receitaPorMes() {
    Map<YearMonth, double[]> receitas = new TreeMap<>();

    // Receita de locações (usa o preço do jogo como valor da locação)
    for (Aluguel aluguel : repositorioAlugueis.listar()) {
      if (aluguel.getDataInicio() == null) {
        continue;
      }
      YearMonth mes = YearMonth.from(aluguel.getDataInicio());
      double valor = 0.0;
      if (aluguel.getValorLocacao() != null) {
        valor = aluguel.getValorLocacao();
      } else if (aluguel.getJogo() != null && aluguel.getJogo().getPrecoLocacaoDiaria() != null) {
        valor = aluguel.getJogo().getPrecoLocacaoDiaria();
      } else if (aluguel.getJogo() != null && aluguel.getJogo().getPreco() != null) {
        valor = aluguel.getJogo().getPreco();
      }
      double[] rec = receitas.computeIfAbsent(mes, k -> new double[3]);
      rec[0] += valor;
    }

    // Receita de vendas
    for (Venda venda : repositorioVendas.listar()) {
      if (venda.getDataVenda() == null) {
        continue;
      }
      YearMonth mes = YearMonth.from(venda.getDataVenda());
      double valor = venda.getValorTotal() != null ? venda.getValorTotal() : 0.0;
      double[] rec = receitas.computeIfAbsent(mes, k -> new double[3]);
      rec[1] += valor;
    }

    // Calcula totais com arredondamento
    for (double[] rec : receitas.values()) {
      rec[0] = arredondar(rec[0]);
      rec[1] = arredondar(rec[1]);
      rec[2] = arredondar(rec[0] + rec[1]);
    }

    return receitas;
  }

  /**
   * Gera uma representação textual do relatório de receita por mês, formatada
   * como tabela legível para exibição.
   *
   * @return relatório formatado como String
   */
  public String gerarRelatorioReceitaPorMes() {
    Map<YearMonth, double[]> receitas = receitaPorMes();
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%-10s | %18s | %18s | %18s%n", "Mês", "Receita Locações",
        "Receita Vendas", "Receita Total"));
    sb.append("-----------|--------------------|--------------------|--------------------\n");

    double totalLocacoes = 0;
    double totalVendas = 0;
    double totalGeral = 0;

    for (Map.Entry<YearMonth, double[]> entrada : receitas.entrySet()) {
      YearMonth mes = entrada.getKey();
      double[] rec = entrada.getValue();
      sb.append(String.format("%-10s | %18.2f | %18.2f | %18.2f%n",
          mes.toString(), rec[0], rec[1], rec[2]));
      totalLocacoes += rec[0];
      totalVendas += rec[1];
      totalGeral += rec[2];
    }

    sb.append("-----------|--------------------|--------------------|--------------------\n");
    sb.append(String.format("%-10s | %18.2f | %18.2f | %18.2f%n", "TOTAL",
        arredondar(totalLocacoes), arredondar(totalVendas), arredondar(totalGeral)));

    return sb.toString();
  }

  private static double arredondar(double valor) {
    return Math.round(valor * 100.0) / 100.0;
  }
}
