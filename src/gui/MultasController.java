package gui;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import negocio.Relogio;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Cliente;
import model.Penalidade;
import negocio.ClienteService;
import negocio.FinanceiroService;

public class MultasController {

    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ClienteService clienteService = Contexto.get().getClienteService();
    private final FinanceiroService financeiroService = Contexto.get().getFinanceiroService();

    // --- Aba Pendentes ---
    @FXML private TableView<Penalidade> tabelaMultas;
    @FXML private TableColumn<Penalidade, String> colCliente;
    @FXML private TableColumn<Penalidade, String> colCpf;
    @FXML private TableColumn<Penalidade, String> colJogo;
    @FXML private TableColumn<Penalidade, String> colGeracao;
    @FXML private TableColumn<Penalidade, String> colValor;
    @FXML private TableColumn<Penalidade, String> colSituacao;
    @FXML private Label rotuloTotal;
    @FXML private Button botaoPagar;

    // --- Aba Multas Pagas ---
    @FXML private TableView<Penalidade> tabelaPagas;
    @FXML private TableColumn<Penalidade, String> colClientePaga;
    @FXML private TableColumn<Penalidade, String> colCpfPaga;
    @FXML private TableColumn<Penalidade, String> colJogoPaga;
    @FXML private TableColumn<Penalidade, String> colGeracaoPaga;
    @FXML private TableColumn<Penalidade, String> colValorPaga;
    @FXML private TableColumn<Penalidade, String> colPagaEm;
    @FXML private Label rotuloTotalPago;

    @FXML
    public void initialize() {
        // Configurar colunas da aba Pendentes
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAluguelOrigem().getCliente().getNome()));
        colCpf.setCellValueFactory(c -> new SimpleStringProperty(
                Formatadores.formatarCpf(c.getValue().getAluguelOrigem().getCliente().getCpf())));
        colJogo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAluguelOrigem().getJogo().getTitulo()));
        colGeracao.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataGeracao() == null ? "-" : c.getValue().getDataGeracao().format(DATA)));
        colValor.setCellValueFactory(c -> new SimpleStringProperty(dinheiro(c.getValue().getValor())));
        // antes era sempre "Pendente" (repetia o nome da aba); agora diz se já dá para pagar
        colSituacao.setCellValueFactory(c -> new SimpleStringProperty(
                jogoDevolvido(c.getValue()) ? "Pronta para pagamento" : "Aguardando devolução"));
        // o FinanceiroService só aceita pagar depois que o jogo foi devolvido (a multa ainda cresce)
        botaoPagar.disableProperty().bind(javafx.beans.binding.Bindings.createBooleanBinding(
                () -> !jogoDevolvido(tabelaMultas.getSelectionModel().getSelectedItem()),
                tabelaMultas.getSelectionModel().selectedItemProperty()));

        colGeracao.setComparator(Ordenacao.DATA_BR);
        colValor.setComparator(Ordenacao.DINHEIRO);

        // Configurar colunas da aba Multas Pagas
        colClientePaga.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAluguelOrigem().getCliente().getNome()));
        colCpfPaga.setCellValueFactory(c -> new SimpleStringProperty(
                Formatadores.formatarCpf(c.getValue().getAluguelOrigem().getCliente().getCpf())));
        colJogoPaga.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAluguelOrigem().getJogo().getTitulo()));
        colGeracaoPaga.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataGeracao() == null ? "-" : c.getValue().getDataGeracao().format(DATA)));
        colValorPaga.setCellValueFactory(c -> new SimpleStringProperty(dinheiro(c.getValue().getValor())));
        colPagaEm.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataPagamento() == null ? "-" : c.getValue().getDataPagamento().format(DATA)));
        colGeracaoPaga.setComparator(Ordenacao.DATA_BR);
        colValorPaga.setComparator(Ordenacao.DINHEIRO);
        colPagaEm.setComparator(Ordenacao.DATA_BR);

        atualizar();
    }

    private void atualizar() {
        List<Penalidade> pendentes = new ArrayList<>();
        List<Penalidade> pagas = new ArrayList<>();
        double totalPendente = 0.0;
        double totalPago = 0.0;

        for (Cliente cliente : clienteService.listar()) {
            for (Penalidade p : financeiroService.listarMultas(cliente)) {
                if (p.isPaga()) {
                    pagas.add(p);
                    totalPago += p.getValor() == null ? 0.0 : p.getValor();
                } else {
                    pendentes.add(p);
                    totalPendente += p.getValor() == null ? 0.0 : p.getValor();
                }
            }
        }

        tabelaMultas.getItems().setAll(pendentes);
        rotuloTotal.setText("Total pendente: " + dinheiro(Math.round(totalPendente * 100.0) / 100.0));

        tabelaPagas.getItems().setAll(pagas);
        rotuloTotalPago.setText("Total pago: " + dinheiro(Math.round(totalPago * 100.0) / 100.0));
    }

    @FXML
    private void aoPagar() {
        Penalidade selecionada = tabelaMultas.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            Alertas.avisar("Selecione uma multa para registrar o pagamento.");
            return;
        }
        if (!Alertas.confirmar("Registrar pagamento", "Registrar o pagamento de " + dinheiro(selecionada.getValor())
                + " de \"" + selecionada.getAluguelOrigem().getCliente().getNome() + "\"?")) {
            return;
        }
        try {
            financeiroService.pagarMulta(selecionada, Relogio.hoje());
            atualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.avisar(e.getMessage());
        }
    }

    private static boolean jogoDevolvido(Penalidade multa) {
        return multa != null && multa.getAluguelOrigem() != null
                && multa.getAluguelOrigem().getDataDevolucaoEfetiva() != null;
    }

    private static String dinheiro(Double valor) {
        return valor == null ? "-" : String.format(BR, "R$ %.2f", valor);
    }
}
