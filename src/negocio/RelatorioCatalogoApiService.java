package negocio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dados.IRepositorioJogos;
import dados.RepositorioJogosLista;
import model.Jogo;

/**
 * REQ16 – Listar jogos importados da API por gênero, para conferência do catálogo sincronizado.
 * Depende exclusivamente do repositório de jogos.
 */
public class RelatorioCatalogoApiService {

  // Singleton: uma única instância em memória; use RelatorioCatalogoApiService.getInstance().
  private static final RelatorioCatalogoApiService INSTANCIA = new RelatorioCatalogoApiService(
      RepositorioJogosLista.getInstance());

  public static RelatorioCatalogoApiService getInstance() {
    return INSTANCIA;
  }

  private final IRepositorioJogos repositorioJogos;

  public RelatorioCatalogoApiService(IRepositorioJogos repositorioJogos) {
    if (repositorioJogos == null) {
      throw new IllegalArgumentException("O repositório de jogos é obrigatório.");
    }
    this.repositorioJogos = repositorioJogos;
  }

  /**
   * Agrupa os jogos importados da API por gênero, considerando sem gênero como "Outros".
   * Os gêneros são ordenados sem diferenciar maiúsculas de minúsculas.
   *
   * @return mapa e listas imutáveis com os jogos importados agrupados por gênero
   */
  public Map<String, List<Jogo>> listarTodosImportadosAgrupadosPorGenero() {
    Map<String, List<Jogo>> agrupamento = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    for (Jogo jogo : repositorioJogos.listar()) {
      if (isImportadoDaApi(jogo)) {
        String genero = normalizarGenero(jogo.getGenero());
        agrupamento.computeIfAbsent(genero, chave -> new ArrayList<>()).add(jogo);
      }
    }

    Map<String, List<Jogo>> resultado = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    agrupamento.forEach((genero, jogos) ->
        resultado.put(genero, Collections.unmodifiableList(new ArrayList<>(jogos))));
    return Collections.unmodifiableMap(resultado);
  }

  /**
   * Lista os jogos importados da API filtrados por um gênero específico.
   * A busca ignora espaços externos e maiúsculas/minúsculas.
   *
   * @param genero gênero a ser pesquisado (ex: "Shooter", "MMORPG", "Outros")
   * @return lista imutável dos jogos correspondentes
   */
  public List<Jogo> listarImportadosPorGenero(String genero) {
    if (genero == null || genero.trim().isEmpty()) {
      throw new IllegalArgumentException("O gênero é obrigatório.");
    }

    String generoPesquisado = normalizarGenero(genero);
    List<Jogo> resultado = new ArrayList<>();
    for (Jogo jogo : repositorioJogos.listar()) {
      if (isImportadoDaApi(jogo) && generoPesquisado.equalsIgnoreCase(normalizarGenero(jogo.getGenero()))) {
        resultado.add(jogo);
      }
    }
    return Collections.unmodifiableList(resultado);
  }

  /** Lista os gêneros distintos dos jogos importados, em ordem alfabética sem distinção de caixa. */
  public List<String> listarGenerosDisponiveis() {
    return Collections.unmodifiableList(
        new ArrayList<>(listarTodosImportadosAgrupadosPorGenero().keySet()));
  }

  /** Gera um relatório textual completo de conferência do catálogo sincronizado. */
  public String gerarRelatorioConferenciaCatalogo() {
    Map<String, List<Jogo>> agrupamento = listarTodosImportadosAgrupadosPorGenero();
    StringBuilder sb = new StringBuilder();

    sb.append("======================================================================\n");
    sb.append("   CONFERÊNCIA DO CATÁLOGO SINCRONIZADO - JOGOS IMPORTADOS DA API     \n");
    sb.append("======================================================================\n\n");

    if (agrupamento.isEmpty()) {
      sb.append("Nenhum jogo sincronizado da API foi encontrado no catálogo.\n");
      return sb.toString();
    }

    int totalGeralJogos = 0;

    for (Map.Entry<String, List<Jogo>> entrada : agrupamento.entrySet()) {
      String genero = entrada.getKey();
      List<Jogo> jogos = entrada.getValue();
      totalGeralJogos += jogos.size();

      sb.append(String.format("► GÊNERO: %s (%d jogo(s))\n", genero.toUpperCase(), jogos.size()));
      sb.append("  " + "-".repeat(66) + "\n");

      for (Jogo jogo : jogos) {
        sb.append(String.format("  • [ID: %s] %s\n", jogo.getId(), jogo.getTitulo()));
        sb.append(String.format("    - Desenvolvedora: %s | Preço: R$ %.2f\n",
            jogo.getDesenvolvedora() != null ? jogo.getDesenvolvedora() : "N/D",
            jogo.getPreco() != null ? jogo.getPreco() : 0.0));
        sb.append(String.format("    - URL de Referência: %s\n",
            jogo.getUrlReferencia() != null ? jogo.getUrlReferencia() : "N/D"));
      }
      sb.append("\n");
    }

    sb.append("======================================================================\n");
    sb.append(String.format("Total de gêneros: %d | Total de jogos importados da API: %d\n",
        agrupamento.size(), totalGeralJogos));
    sb.append("======================================================================\n");

    return sb.toString();
  }

  /** Normaliza categorias para que agrupamento e pesquisa compartilhem a regra "Outros". */
  private static String normalizarGenero(String genero) {
    if (genero == null || genero.trim().isEmpty()) {
      return "Outros";
    }
    return genero.trim();
  }

  /** Um jogo importado possui uma URL de referência cadastrada. */
  private boolean isImportadoDaApi(Jogo jogo) {
    return jogo != null
        && jogo.getUrlReferencia() != null
        && !jogo.getUrlReferencia().trim().isEmpty();
  }
}
