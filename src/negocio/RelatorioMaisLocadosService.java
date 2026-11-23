package negocio;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
   * ordenados por quantidade decrescente e, em caso de empate, por título e ID.
   *
   * @param inicio data inicial do período (inclusiva)
   * @param fim    data final do período (inclusiva)
   * @return lista imutável de pares (jogo, quantidade)
   */
  public List<Map.Entry<Jogo, Long>> jogosMaisLocadosPorPeriodo(LocalDate inicio, LocalDate fim) {
    validarPeriodo(inicio, fim);

    Map<Long, Long> contagemPorJogoId = new LinkedHashMap<>();
    Map<Long, Jogo> jogosPorId = new LinkedHashMap<>();

    for (Aluguel aluguel : repositorioAlugueis.listar()) {
      if (aluguel == null) {
        continue;
      }
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
      resultado.add(new AbstractMap.SimpleImmutableEntry<>(jogo, entrada.getValue()));
    }

    resultado.sort(Comparator
        .<Map.Entry<Jogo, Long>>comparingLong(Map.Entry::getValue)
        .reversed()
        .thenComparing(entry -> entry.getKey().getTitulo(),
            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
        .thenComparing(entry -> entry.getKey().getTitulo(),
            Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(entry -> entry.getKey().getId(), Comparator.nullsLast(Long::compareTo)));

    return Collections.unmodifiableList(resultado);
  }

  /**
   * Gera o conteúdo CSV do relatório de jogos mais locados no período.
   * Campos textuais são protegidos contra fórmulas de planilha e delimitadores CSV.
   *
   * @param inicio data inicial do período (inclusiva)
   * @param fim    data final do período (inclusiva)
   * @return conteúdo CSV como String
   */
  public String gerarCsvJogosMaisLocados(LocalDate inicio, LocalDate fim) {
    List<Map.Entry<Jogo, Long>> ranking = jogosMaisLocadosPorPeriodo(inicio, fim);
    StringBuilder csv = new StringBuilder();
    csv.append("Posição;ID;Título;Gênero;Quantidade de Locações\r\n");

    int posicao = 1;
    for (Map.Entry<Jogo, Long> entrada : ranking) {
      Jogo jogo = entrada.getKey();
      csv.append(posicao++).append(";");
      csv.append(jogo.getId()).append(";");
      csv.append(escaparCsv(jogo.getTitulo())).append(";");
      csv.append(escaparCsv(jogo.getGenero())).append(";");
      csv.append(entrada.getValue()).append("\r\n");
    }

    return csv.toString();
  }

  /**
   * Exporta o relatório de jogos mais locados para um arquivo CSV UTF-8 no caminho informado.
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
    try (BufferedWriter writer = Files.newBufferedWriter(
        Path.of(caminhoArquivo.trim()), StandardCharsets.UTF_8)) {
      writer.write(conteudo);
    }
  }

  /** Gera uma página visual do ranking mensal, usando thumbnails da API quando disponíveis. */
  public void exportarHtmlRankingMensal(LocalDate inicio, LocalDate fim, Path caminhoArquivo)
      throws IOException {
    if (caminhoArquivo == null) {
      throw new IllegalArgumentException("O caminho do arquivo é obrigatório.");
    }
    List<Map.Entry<Jogo, Long>> ranking = jogosMaisLocadosPorPeriodo(inicio, fim);
    StringBuilder html = new StringBuilder("<!doctype html><html lang=\"pt-BR\"><head><meta charset=\"UTF-8\">");
    html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><title>GameVault · Ranking mensal</title>")
        .append("<style>*{box-sizing:border-box}body{margin:0;min-height:100vh;font-family:Arial,sans-serif;color:#f8f7ff;background:radial-gradient(circle at 80% 0,#54208a,#10091d 70%)}main{max-width:1100px;margin:auto;padding:48px 24px 64px}.brand{color:#ff45c7;font-weight:700;letter-spacing:.08em;text-transform:uppercase}h1{font-size:clamp(2rem,5vw,4rem);margin:12px 0 8px}.subtitle{color:#c9bfdf;margin:0 0 34px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(250px,1fr));gap:18px}.card{position:relative;overflow:hidden;border:1px solid #7141aa;border-radius:18px;background:rgba(35,18,61,.86);box-shadow:0 14px 34px #0005}.cover{width:100%;height:150px;object-fit:cover;background:linear-gradient(135deg,#6530a7,#f02fbd)}.body{padding:18px}.rank{position:absolute;top:12px;left:12px;width:38px;height:38px;border-radius:50%;display:grid;place-items:center;background:#7139ef;font-weight:700}h2{font-size:1.15rem;margin:0 0 8px}.meta{color:#c9bfdf;font-size:.9rem}.count{color:#ff65d2;font-size:1.4rem;font-weight:700;margin-top:16px}.empty{padding:32px;border:1px dashed #7141aa;border-radius:18px;color:#c9bfdf;text-align:center}footer{margin-top:36px;color:#9688b2;font-size:.82rem}</style></head><body><main>")
        .append("<div class=\"brand\">GameVault</div><h1>Ranking do mês</h1><p class=\"subtitle\">Jogos mais locados de ")
        .append(escapeHtml(inicio.toString())).append(" a ").append(escapeHtml(fim.toString())).append("</p>");
    if (ranking.isEmpty()) {
      html.append("<div class=\"empty\">Ainda não há locações no período selecionado.</div>");
    } else {
      html.append("<section class=\"grid\" aria-label=\"Jogos mais locados\">");
      int posicao = 1;
      for (Map.Entry<Jogo, Long> entrada : ranking) {
        Jogo jogo = entrada.getKey();
        html.append("<article class=\"card\"><div class=\"rank\">").append(posicao++).append("</div>");
        if (jogo.getUrlThumbnail() != null && !jogo.getUrlThumbnail().isBlank()) {
          html.append("<img class=\"cover\" loading=\"lazy\" src=\"").append(escapeHtml(jogo.getUrlThumbnail()))
              .append("\" alt=\"Capa de ").append(escapeHtml(jogo.getTitulo())).append("\">");
        } else {
          html.append("<div class=\"cover\" role=\"img\" aria-label=\"Capa indisponível\"></div>");
        }
        html.append("<div class=\"body\"><h2>").append(escapeHtml(jogo.getTitulo())).append("</h2><div class=\"meta\">")
            .append(escapeHtml(jogo.getGenero())).append("</div><div class=\"count\">").append(entrada.getValue())
            .append(entrada.getValue() == 1 ? " locação" : " locações").append("</div></div></article>");
      }
      html.append("</section>");
    }
    html.append("<footer>Gerado pelo GameVault</footer></main></body></html>");
    Path pai = caminhoArquivo.toAbsolutePath().getParent();
    if (pai != null) Files.createDirectories(pai);
    Files.writeString(caminhoArquivo, html.toString(), StandardCharsets.UTF_8);
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
    String seguro = neutralizarFormula(valor);
    if (seguro.contains(";") || seguro.contains("\"") || seguro.contains("\n") || seguro.contains("\r")) {
      return "\"" + seguro.replace("\"", "\"\"") + "\"";
    }
    return seguro;
  }

  private static String escapeHtml(String valor) {
    if (valor == null) return "";
    return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&#39;");
  }

  /** Evita que textos importados sejam executados como fórmulas ao abrir o CSV em planilhas. */
  private static String neutralizarFormula(String valor) {
    for (int i = 0; i < valor.length(); i++) {
      char caractere = valor.charAt(i);
      if (!Character.isWhitespace(caractere) && !Character.isISOControl(caractere)) {
        return "=+-@".indexOf(caractere) >= 0 ? "'" + valor : valor;
      }
    }
    return valor;
  }
}
