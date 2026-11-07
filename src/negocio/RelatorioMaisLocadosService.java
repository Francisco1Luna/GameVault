package negocio;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dados.IRepositorioAlugueis;
import dados.RepositorioAlugueisLista;
import model.Aluguel;
import model.Jogo;

/**
 * REQ14 – Relatório dos jogos mais locados por período, exportável em CSV.
 * Depende exclusivamente do repositório de aluguéis.
 */
public class RelatorioMaisLocadosService {

  // Singleton: uma única instância em memória; use RelatorioMaisLocadosService.getInstance().
  private static final RelatorioMaisLocadosService INSTANCIA = new RelatorioMaisLocadosService(
      RepositorioAlugueisLista.getInstance());

  public static RelatorioMaisLocadosService getInstance() {
    return INSTANCIA;
  }

  private final IRepositorioAlugueis repositorioAlugueis;

  public RelatorioMaisLocadosService(IRepositorioAlugueis repositorioAlugueis) {
    if (repositorioAlugueis == null) {
      throw new IllegalArgumentException("O repositório de aluguéis é obrigatório.");
    }
    this.repositorioAlugueis = repositorioAlugueis;
  }

  /**
   * Retorna os jogos mais locados dentro do período informado (início e fim inclusivos),
   * ordenados do mais locado para o menos locado.
   *
   * @param inicio data inicial do período (inclusiva)
   * @param fim    data final do período (inclusiva)
   * @return lista de pares (jogo, quantidade) ordenada por quantidade decrescente
   */
  public List<Map.Entry<Jogo, Long>> jogosMaisLocadosPorPeriodo(LocalDate inicio, LocalDate fim) {
    validarPeriodo(inicio, fim);

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

    List<Map.Entry<Jogo, Long>> resultado = new ArrayList<>();
    for (Map.Entry<Long, Long> entrada : contagemPorJogoId.entrySet()) {
      Jogo jogo = jogosPorId.get(entrada.getKey());
      resultado.add(new AbstractMap.SimpleEntry<>(jogo, entrada.getValue()));
    }

    resultado.sort(new Comparator<Map.Entry<Jogo, Long>>() {
      @Override
      public int compare(Map.Entry<Jogo, Long> a, Map.Entry<Jogo, Long> b) {
        return Long.compare(b.getValue(), a.getValue());
      }
    });

    return Collections.unmodifiableList(resultado);
  }

  /**
   * Gera o conteúdo CSV do relatório de jogos mais locados no período.
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

  private static void validarPeriodo(LocalDate inicio, LocalDate fim) {
    if (inicio == null || fim == null) {
      throw new IllegalArgumentException("As datas de início e fim do período são obrigatórias.");
    }
    if (fim.isBefore(inicio)) {
      throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
    }
  }

  private static String escaparCsv(String valor) {
    if (valor == null) {
      return "";
    }
    if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
      return "\"" + valor.replace("\"", "\"\"") + "\"";
    }
    return valor;
  }
}
