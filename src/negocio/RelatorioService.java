package negocio;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dados.IRepositorioAlugueis;
import dados.IRepositorioJogos;
import dados.IRepositorioVendas;
import model.Aluguel;
import model.Jogo;
import model.Venda;

/**
 * REQ14 – Relatório dos jogos mais locados por período, exportável em CSV.
 * REQ15 – Relatório de receita (locações + vendas) por mês.
 * REQ16 – Listar jogos importados da API por gênero, para conferência do catálogo sincronizado.
 */
public class RelatorioService {

  private final IRepositorioAlugueis repositorioAlugueis;
  private final IRepositorioJogos repositorioJogos;
  private final IRepositorioVendas repositorioVendas;

  public RelatorioService(IRepositorioAlugueis repositorioAlugueis,
      IRepositorioJogos repositorioJogos,
      IRepositorioVendas repositorioVendas) {
    if (repositorioAlugueis == null) {
      throw new IllegalArgumentException("O repositório de aluguéis é obrigatório.");
    }
    if (repositorioJogos == null) {
      throw new IllegalArgumentException("O repositório de jogos é obrigatório.");
    }
    if (repositorioVendas == null) {
      throw new IllegalArgumentException("O repositório de vendas é obrigatório.");
    }
    this.repositorioAlugueis = repositorioAlugueis;
    this.repositorioJogos = repositorioJogos;
    this.repositorioVendas = repositorioVendas;
  }

  // ================================================================== REQ14

  /**
   * Retorna os jogos mais locados dentro do período informado (início e fim inclusivos),
   * ordenados do mais locado para o menos locado. Cada entrada do mapa contém o jogo e a
   * quantidade de locações realizadas no período.
   *
   * @param inicio data inicial do período (inclusiva)
   * @param fim    data final do período (inclusiva)
   * @return lista de pares (jogo, quantidade) ordenada por quantidade decrescente
   */
  public List<Map.Entry<Jogo, Long>> jogosMaisLocadosPorPeriodo(LocalDate inicio, LocalDate fim) {
    validarPeriodo(inicio, fim);

    // Conta locações por jogo no período
    Map<Long, Long> contagemPorJogoId = new LinkedHashMap<>();
    Map<Long, Jogo> jogosPorId = new LinkedHashMap<>();

    for (Aluguel aluguel : repositorioAlugueis.listar()) {
      LocalDate dataLocacao = aluguel.getDataInicio();
      if (dataLocacao != null && !dataLocacao.isBefore(inicio) && !dataLocacao.isAfter(fim)) {
        Jogo jogo = aluguel.getJogo();
        if (jogo != null && jogo.getId() != null) {
          Long jogoId = jogo.getId();
          contagemPorJogoId.put(jogoId, contagemPorJogoId.getOrDefault(jogoId, 0L) + 1);
          jogosPorId.putIfAbsent(jogoId, jogo);
        }
      }
    }

    // Monta a lista de resultados e ordena por contagem decrescente
    List<Map.Entry<Jogo, Long>> resultado = new ArrayList<>();
    for (Map.Entry<Long, Long> entrada : contagemPorJogoId.entrySet()) {
      Jogo jogo = jogosPorId.get(entrada.getKey());
      resultado.add(new java.util.AbstractMap.SimpleEntry<>(jogo, entrada.getValue()));
    }

    resultado.sort(new Comparator<Map.Entry<Jogo, Long>>() {
      @Override
      public int compare(Map.Entry<Jogo, Long> a, Map.Entry<Jogo, Long> b) {
        return Long.compare(b.getValue(), a.getValue()); // decrescente
      }
    });

    return Collections.unmodifiableList(resultado);
  }

  /**
   * Gera o conteúdo CSV do relatório de jogos mais locados no período.
   * O CSV inclui cabeçalho e uma linha por jogo, com as colunas:
   * Posição, ID do Jogo, Título, Gênero, Quantidade de Locações.
   *
   * @param inicio data inicial do período (inclusiva)
   * @param fim    data final do período (inclusiva)
   * @return conteúdo CSV como String
   */
  public String gerarCsvJogosMaisLocados(LocalDate inicio, LocalDate fim) {
    List<Map.Entry<Jogo, Long>> ranking = jogosMaisLocadosPorPeriodo(inicio, fim);
    StringBuilder csv = new StringBuilder();
    csv.append("Posicao;ID;Titulo;Genero;Quantidade de Locacoes\n");

    int posicao = 1;
    for (Map.Entry<Jogo, Long> entrada : ranking) {
      Jogo jogo = entrada.getKey();
      csv.append(posicao++).append(";");
      csv.append(jogo.getId()).append(";");
      csv.append(escaparCsv(jogo.getTitulo())).append(";");
      csv.append(escaparCsv(jogo.getGenero())).append(";");
      csv.append(entrada.getValue()).append("\n");
    }

    return csv.toString();
  }

  /**
   * Exporta o relatório de jogos mais locados para um arquivo CSV no caminho informado.
   *
   * @param inicio         data inicial do período (inclusiva)
   * @param fim            data final do período (inclusiva)
   * @param caminhoArquivo caminho completo do arquivo CSV a ser criado
   * @throws IOException se houver erro ao escrever o arquivo
   */
  public void exportarCsvJogosMaisLocados(LocalDate inicio, LocalDate fim, String caminhoArquivo)
      throws IOException {
    if (caminhoArquivo == null || caminhoArquivo.trim().isEmpty()) {
      throw new IllegalArgumentException("O caminho do arquivo é obrigatório.");
    }
    String conteudo = gerarCsvJogosMaisLocados(inicio, fim);
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminhoArquivo))) {
      writer.write(conteudo);
    }
  }

  // ================================================================== REQ15

  /**
   * Calcula a receita total por mês, somando receita de locações e de vendas.
   * Retorna um mapa ordenado por mês (YearMonth), onde cada entrada contém um array
   * de 3 valores: [receitaLocacoes, receitaVendas, receitaTotal].
   *
   * <p>A receita de cada locação é o preço do jogo ({@code jogo.getPreco()}).
   * A receita de cada venda é o valor total da venda ({@code venda.getValorTotal()}).
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
      if (aluguel.getJogo() != null && aluguel.getJogo().getPreco() != null) {
        valor = aluguel.getJogo().getPreco();
      }
      double[] rec = receitas.computeIfAbsent(mes, k -> new double[3]);
      rec[0] += valor; // receitaLocacoes
    }

    // Receita de vendas
    for (Venda venda : repositorioVendas.listar()) {
      if (venda.getDataVenda() == null) {
        continue;
      }
      YearMonth mes = YearMonth.from(venda.getDataVenda());
      double valor = venda.getValorTotal() != null ? venda.getValorTotal() : 0.0;
      double[] rec = receitas.computeIfAbsent(mes, k -> new double[3]);
      rec[1] += valor; // receitaVendas
    }

    // Calcula totais
    for (double[] rec : receitas.values()) {
      rec[0] = arredondar(rec[0]);
      rec[1] = arredondar(rec[1]);
      rec[2] = arredondar(rec[0] + rec[1]); // receitaTotal
    }

    return receitas;
  }

  /**
   * Gera uma representação textual do relatório de receita por mês, formatada
   * como tabela legível para exibição no console.
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

  // ================================================================== REQ16

  /**
   * Lista os jogos importados da API (identificados por possuírem {@code urlReferencia}
   * preenchida) filtrados pelo gênero informado. A comparação de gênero ignora
   * maiúsculas/minúsculas.
   *
   * @param genero o gênero a ser filtrado (ex: "MMORPG", "Shooter")
   * @return lista de jogos importados da API que pertencem ao gênero informado
   */
  public List<Jogo> listarJogosImportadosPorGenero(String genero) {
    if (genero == null || genero.trim().isEmpty()) {
      throw new IllegalArgumentException("O gênero é obrigatório.");
    }

    List<Jogo> resultado = new ArrayList<>();
    for (Jogo jogo : repositorioJogos.listar()) {
      // Jogo importado da API = possui urlReferencia preenchida
      if (jogo.getUrlReferencia() != null && !jogo.getUrlReferencia().trim().isEmpty()) {
        if (genero.equalsIgnoreCase(jogo.getGenero())) {
          resultado.add(jogo);
        }
      }
    }
    return resultado;
  }

  /**
   * Lista todos os gêneros distintos dos jogos importados da API, em ordem alfabética.
   * Útil para saber quais gêneros estão disponíveis antes de filtrar com
   * {@link #listarJogosImportadosPorGenero(String)}.
   *
   * @return lista de gêneros disponíveis nos jogos importados
   */
  public List<String> listarGenerosDisponiveis() {
    List<String> generos = new ArrayList<>();
    for (Jogo jogo : repositorioJogos.listar()) {
      if (jogo.getUrlReferencia() != null && !jogo.getUrlReferencia().trim().isEmpty()) {
        String genero = jogo.getGenero();
        if (genero != null && !genero.trim().isEmpty() && !generos.contains(genero)) {
          generos.add(genero);
        }
      }
    }
    Collections.sort(generos);
    return generos;
  }

  // ================================================================== apoio

  private static void validarPeriodo(LocalDate inicio, LocalDate fim) {
    if (inicio == null || fim == null) {
      throw new IllegalArgumentException("As datas de início e fim do período são obrigatórias.");
    }
    if (fim.isBefore(inicio)) {
      throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
    }
  }

  /** Escapa campos CSV que contenham ponto-e-vírgula ou aspas. */
  private static String escaparCsv(String valor) {
    if (valor == null) {
      return "";
    }
    if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
      return "\"" + valor.replace("\"", "\"\"") + "\"";
    }
    return valor;
  }

  private static double arredondar(double valor) {
    return Math.round(valor * 100.0) / 100.0;
  }
}
