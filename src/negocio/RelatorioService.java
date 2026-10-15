package negocio;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import dados.IRepositorioAlugueis;
import dados.IRepositorioJogos;
import dados.IRepositorioVendas;
import model.Jogo;

/**
 * Fachada (Facade) unificada para acesso aos relatórios do sistema GameVault:
 * - REQ14: {@link RelatorioMaisLocadosService}
 * - REQ15: {@link RelatorioReceitaService}
 * - REQ16: {@link RelatorioCatalogoApiService}
 */
public class RelatorioService {

  private final RelatorioMaisLocadosService maisLocadosService;
  private final RelatorioReceitaService receitaService;
  private final RelatorioCatalogoApiService catalogoApiService;

  public RelatorioService(IRepositorioAlugueis repositorioAlugueis,
      IRepositorioJogos repositorioJogos,
      IRepositorioVendas repositorioVendas) {
    this(new RelatorioMaisLocadosService(repositorioAlugueis),
        new RelatorioReceitaService(repositorioAlugueis, repositorioVendas),
        new RelatorioCatalogoApiService(repositorioJogos));
  }

  public RelatorioService(RelatorioMaisLocadosService maisLocadosService,
      RelatorioReceitaService receitaService,
      RelatorioCatalogoApiService catalogoApiService) {
    if (maisLocadosService == null || receitaService == null || catalogoApiService == null) {
      throw new IllegalArgumentException("Os serviços especializados de relatório são obrigatórios.");
    }
    this.maisLocadosService = maisLocadosService;
    this.receitaService = receitaService;
    this.catalogoApiService = catalogoApiService;
  }

  // --- REQ14 ---

  public List<Map.Entry<Jogo, Long>> jogosMaisLocadosPorPeriodo(LocalDate inicio, LocalDate fim) {
    return maisLocadosService.jogosMaisLocadosPorPeriodo(inicio, fim);
  }

  public String gerarCsvJogosMaisLocados(LocalDate inicio, LocalDate fim) {
    return maisLocadosService.gerarCsvJogosMaisLocados(inicio, fim);
  }

  public void exportarCsvJogosMaisLocados(LocalDate inicio, LocalDate fim, String caminhoArquivo)
      throws IOException {
    maisLocadosService.exportarCsvJogosMaisLocados(inicio, fim, caminhoArquivo);
  }

  // --- REQ15 ---

  public Map<YearMonth, double[]> receitaPorMes() {
    return receitaService.receitaPorMes();
  }

  public String gerarRelatorioReceitaPorMes() {
    return receitaService.gerarRelatorioReceitaPorMes();
  }

  // --- REQ16 ---

  public Map<String, List<Jogo>> listarTodosImportadosAgrupadosPorGenero() {
    return catalogoApiService.listarTodosImportadosAgrupadosPorGenero();
  }

  public List<Jogo> listarJogosImportadosPorGenero(String genero) {
    return catalogoApiService.listarImportadosPorGenero(genero);
  }

  public List<String> listarGenerosDisponiveis() {
    return catalogoApiService.listarGenerosDisponiveis();
  }

  public String gerarRelatorioConferenciaCatalogo() {
    return catalogoApiService.gerarRelatorioConferenciaCatalogo();
  }

  // --- Getters para acesso aos serviços individuais ---

  public RelatorioMaisLocadosService getMaisLocadosService() {
    return maisLocadosService;
  }

  public RelatorioReceitaService getReceitaService() {
    return receitaService;
  }

  public RelatorioCatalogoApiService getCatalogoApiService() {
    return catalogoApiService;
  }
}
