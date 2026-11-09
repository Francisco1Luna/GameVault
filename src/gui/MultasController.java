package gui;

import java.time.LocalDate;
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

    @FXML private TableView<Penalidade> tabelaMultas;
    @FXML private TableColumn<Penalidade, String> colCliente;
    @FXML private TableColumn<Penalidade, String> colCpf;
    @FXML private TableColumn<Penalidade, String> colJogo;
    @FXML private TableColumn<Penalidade, String> colGeracao;
    @FXML private TableColumn<Penalidade, String> colValor;
    @FXML private Label rotuloTotal;

    @FXML
    public void initialize() {
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAluguelOrigem().getCliente().getNome()));
        colCpf.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAluguelOrigem().getCliente().getCpf()));
        colJogo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAluguelOrigem().getJogo().getTitulo()));
        colGeracao.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataGeracao() == null ? "-" : c.getValue().getDataGeracao().format(DATA)));
        colValor.setCellValueFactory(c -> new SimpleStringProperty(dinheiro(c.getValue().getValor())));
        atualizar();
    }

    private void atualizar() {
        List<Penalidade> pendentes = new ArrayList<>();
        double total = 0.0;
        for (Cliente cliente : clienteService.listar()) {
            for (Penalidade p : financeiroService.consultarPendencias(cliente)) {
                pendentes.add(p);
                total += p.getValor() == null ? 0.0 : p.getValor();
            }
        }
        tabelaMultas.getItems().setAll(pendentes);
        rotuloTotal.setText("Total pendente: " + dinheiro(Math.round(total * 100.0) / 100.0));
    }

    @FXML
    private void aoPagar() {
        Penalidade selecionada = tabelaMultas.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione uma multa para registrar o pagamento.").showAndWait();
            return;
        }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Registrar o pagamento de " + dinheiro(selecionada.getValor()) + " de \""
                        + selecionada.getAluguelOrigem().getCliente().getNome() + "\"?");
        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            financeiroService.pagarMulta(selecionada, Relogio.hoje());
            atualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.avisar(e.getMessage());        }
    }

    private static String dinheiro(Double valor) {
        return valor == null ? "-" : String.format(BR, "R$ %.2f", valor);
    }
}