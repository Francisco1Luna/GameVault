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
   * Agrupa TODOS os jogos importados da API por gênero.
   * Os gêneros são ordenados em ordem alfabética através de um TreeMap.
   * Jogos importados são aqueles que possuem {@code urlReferencia} preenchida.
   *
   * @return mapa onde cada chave é o gênero e o valor é a lista de jogos daquele gênero
   */
  public Map<String, List<Jogo>> listarTodosImportadosAgrupadosPorGenero() {
    Map<String, List<Jogo>> agrupamento = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    for (Jogo jogo : repositorioJogos.listar()) {
      if (isImportadoDaApi(jogo)) {
        String genero = (jogo.getGenero() != null && !jogo.getGenero().trim().isEmpty())
            ? jogo.getGenero().trim()
            : "Outros";

        agrupamento.computeIfAbsent(genero, k -> new ArrayList<>()).add(jogo);
      }
    }

    return agrupamento;
  }

  /**
   * Lista os jogos importados da API filtrados por um gênero específico.
   * A busca ignora maiúsculas e minúsculas.
   *
   * @param genero gênero a ser pesquisado (ex: "Shooter", "MMORPG")
   * @return lista de jogos do gênero correspondente
   */
  public List<Jogo> listarImportadosPorGenero(String genero) {
    if (genero == null || genero.trim().isEmpty()) {
      throw new IllegalArgumentException("O gênero é obrigatório.");
    }

    List<Jogo> resultado = new ArrayList<>();
    for (Jogo jogo : repositorioJogos.listar()) {
      if (isImportadoDaApi(jogo) && genero.equalsIgnoreCase(jogo.getGenero())) {
        resultado.add(jogo);
      }
    }
    return Collections.unmodifiableList(resultado);
  }

  /**
   * Lista todos os gêneros distintos presentes nos jogos importados da API.
   *
   * @return lista ordenada com os nomes dos gêneros
   */
  public List<String> listarGenerosDisponiveis() {
    List<String> generos = new ArrayList<>(listarTodosImportadosAgrupadosPorGenero().keySet());
    Collections.sort(generos);
    return Collections.unmodifiableList(generos);
  }

  /**
   * Gera um relatório textual completo de conferência do catálogo sincronizado,
   * exibindo todos os gêneros e todos os jogos importados da API sob cada gênero.
   *
   * @return texto formatado do relatório para exibição no console ou interface
   */
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

  /**
   * Um jogo é considerado importado da API quando possui uma URL de referência cadastrada.
   */
  private boolean isImportadoDaApi(Jogo jogo) {
    return jogo != null
        && jogo.getUrlReferencia() != null
        && !jogo.getUrlReferencia().trim().isEmpty();
  }
}
