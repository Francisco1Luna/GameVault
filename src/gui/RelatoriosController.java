package gui;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.awt.Desktop;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import model.Jogo;
import model.ItemVenda;
import model.Venda;
import negocio.RelatorioReceitaService.ResumoMensal;
import negocio.RelatorioService;
import negocio.Relogio;

/** Tela de relatórios (REQ14 a REQ16): só exibe o que o RelatorioService já calcula. */
public class RelatoriosController {

    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final String TODOS = "Todos";

    private final RelatorioService relatorioService = Contexto.get().getRelatorioService();
    private final negocio.VendaService vendaService = Contexto.get().getVendaService();

    @FXML private DatePicker campoInicio;
    @FXML private DatePicker campoFim;
    @FXML private TableView<Map.Entry<Jogo, Long>> tabelaMaisLocados;
    @FXML private TableColumn<Map.Entry<Jogo, Long>, String> colPosicao;
    @FXML private TableColumn<Map.Entry<Jogo, Long>, String> colJogoLocado;
    @FXML private TableColumn<Map.Entry<Jogo, Long>, String> colGeneroLocado;
    @FXML private TableColumn<Map.Entry<Jogo, Long>, String> colQuantidade;

    @FXML private TableView<Map.Entry<YearMonth, ResumoMensal>> tabelaReceita;
    @FXML private TableColumn<Map.Entry<YearMonth, ResumoMensal>, String> colMes;
    @FXML private TableColumn<Map.Entry<YearMonth, ResumoMensal>, String> colReceitaLocacoes;
    @FXML private TableColumn<Map.Entry<YearMonth, ResumoMensal>, String> colReceitaVendas;
    @FXML private TableColumn<Map.Entry<YearMonth, ResumoMensal>, String> colReceitaTotal;

    @FXML private TableView<Venda> tabelaVendas;
    @FXML private TableColumn<Venda, String> colVendaId;
    @FXML private TableColumn<Venda, String> colVendaData;
    @FXML private TableColumn<Venda, String> colVendaCliente;
    @FXML private TableColumn<Venda, String> colVendaItens;
    @FXML private TableColumn<Venda, String> colVendaTotal;

    @FXML private ComboBox<String> campoGenero;
    @FXML private TableView<Jogo> tabelaImportados;
    @FXML private TableColumn<Jogo, String> colTitulo;
    @FXML private TableColumn<Jogo, String> colGenero;
    @FXML private TableColumn<Jogo, String> colDesenvolvedora;

    @FXML
    public void initialize() {
        // REQ14: período padrão = do 1º dia do mês até hoje (data do sistema)
        Formatadores.data(campoInicio);
        Formatadores.data(campoFim);
        LocalDate hoje = Relogio.hoje();
        campoInicio.setValue(hoje.withDayOfMonth(1));
        campoFim.setValue(hoje);
        colPosicao.setCellValueFactory(c -> new SimpleStringProperty(
                String.valueOf(tabelaMaisLocados.getItems().indexOf(c.getValue()) + 1)));
        colJogoLocado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKey().getTitulo()));
        colGeneroLocado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKey().getGenero()));
        colQuantidade.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getValue())));
        aoGerarMaisLocados();

        // REQ15
        colMes.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKey().format(MES)));
        colReceitaLocacoes.setCellValueFactory(c -> new SimpleStringProperty(dinheiro(c.getValue().getValue().receitaLocacoes())));
        colReceitaVendas.setCellValueFactory(c -> new SimpleStringProperty(dinheiro(c.getValue().getValue().receitaVendas())));
        colReceitaTotal.setCellValueFactory(c -> new SimpleStringProperty(dinheiro(c.getValue().getValue().receitaTotal())));
        colMes.setComparator(Ordenacao.MES_ANO);
        colReceitaLocacoes.setComparator(Ordenacao.DINHEIRO);
        colReceitaVendas.setComparator(Ordenacao.DINHEIRO);
        colReceitaTotal.setComparator(Ordenacao.DINHEIRO);
        tabelaReceita.getItems().setAll(new ArrayList<>(relatorioService.resumoReceitaPorMes().entrySet()));

        // Saídas de vendas: cada linha representa uma venda registrada, incluindo os itens vendidos.
        colVendaId.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getId() == null ? "-" : String.valueOf(c.getValue().getId())));
        colVendaData.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataVenda() == null ? "-" : c.getValue().getDataVenda().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        colVendaCliente.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCliente() == null ? "-" : c.getValue().getCliente().getNome()));
        colVendaItens.setCellValueFactory(c -> new SimpleStringProperty(textoItens(c.getValue())));
        colVendaTotal.setCellValueFactory(c -> new SimpleStringProperty(dinheiro(c.getValue().getValorTotal())));
        colVendaData.setComparator(String.CASE_INSENSITIVE_ORDER);
        colVendaTotal.setComparator(Ordenacao.DINHEIRO);
        tabelaVendas.getItems().setAll(vendaService.listar());

        // REQ16
        colTitulo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitulo()));
        colGenero.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGenero()));
        colDesenvolvedora.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDesenvolvedora()));
        campoGenero.getItems().add(TODOS);
        campoGenero.getItems().addAll(relatorioService.listarGenerosDisponiveis());
        campoGenero.setValue(TODOS);
        aoFiltrarGenero();
    }

    @FXML
    private void aoGerarMaisLocados() {
        try {
            tabelaMaisLocados.getItems().setAll(
                    relatorioService.jogosMaisLocadosPorPeriodo(campoInicio.getValue(), campoFim.getValue()));
        } catch (IllegalArgumentException e) {
            Alertas.avisar(e.getMessage());
        }
    }

    @FXML
    private void aoExportarCsv() {
        FileChooser escolha = new FileChooser();
        escolha.setTitle("Exportar jogos mais locados");
        escolha.setInitialFileName("jogos_mais_locados.csv");
        escolha.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File arquivo = escolha.showSaveDialog(tabelaMaisLocados.getScene().getWindow());
        if (arquivo == null) {
            return; // cancelado
        }
        try {
            relatorioService.exportarCsvJogosMaisLocados(campoInicio.getValue(), campoFim.getValue(),
                    arquivo.getAbsolutePath());
            Alertas.informar("Relatório exportado para " + arquivo.getAbsolutePath());
        } catch (IllegalArgumentException | IOException e) {
            Alertas.avisar("Não foi possível exportar: " + e.getMessage());
        }
    }

    @FXML
    private void aoAbrirRankingHtml() {
        try {
            Path arquivo = Path.of(System.getProperty("user.home"), "GameVault", "ranking-jogos-mais-locados.html");
            relatorioService.getMaisLocadosService().exportarHtmlRankingMensal(campoInicio.getValue(), campoFim.getValue(), arquivo);
            if (!Desktop.isDesktopSupported()) {
                Alertas.informar("Ranking HTML gerado em " + arquivo.toAbsolutePath());
                return;
            }
            Desktop.getDesktop().browse(arquivo.toUri());
        } catch (IllegalArgumentException | IOException | UnsupportedOperationException e) {
            Alertas.avisar("Não foi possível gerar o ranking HTML: " + e.getMessage());
        }
    }

    @FXML
    private void aoFiltrarGenero() {
        String genero = campoGenero.getValue();
        List<Jogo> jogos = new ArrayList<>();
        if (genero == null || TODOS.equals(genero)) {
            relatorioService.listarTodosImportadosAgrupadosPorGenero().values().forEach(jogos::addAll);
        } else {
            jogos.addAll(relatorioService.listarJogosImportadosPorGenero(genero));
        }
        tabelaImportados.getItems().setAll(jogos);
    }

    private static String dinheiro(BigDecimal valor) {
        return valor == null ? "-" : String.format(BR, "R$ %.2f", valor);
    }

    private static String dinheiro(Double valor) {
        return valor == null ? "-" : String.format(BR, "R$ %.2f", valor);
    }

    private static String textoItens(Venda venda) {
        if (venda == null || venda.getItens() == null || venda.getItens().isEmpty()) {
            return "-";
        }
        return venda.getItens().stream().filter(java.util.Objects::nonNull).map(RelatoriosController::textoItem)
                .collect(java.util.stream.Collectors.joining(" · "));
    }

    private static String textoItem(ItemVenda item) {
        String titulo = item.getJogo() == null ? "Jogo" : item.getJogo().getTitulo();
        int quantidade = item.getQuantidade() == null ? 0 : item.getQuantidade();
        return quantidade + "x " + titulo;
    }
}
