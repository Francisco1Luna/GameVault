package gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Cliente;
import negocio.ClienteService;

public class ClientesController {

    private final ClienteService clienteService = Contexto.get().getClienteService();

    @FXML private TableView<Cliente> tabelaClientes;
    @FXML private TableColumn<Cliente, Long> colId;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colCpf;
    @FXML private TextField campoNome;
    @FXML private TextField campoCpf;

    @FXML
    public void initialize() {
        Formatadores.cpf(campoCpf);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCpf.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                Formatadores.formatarCpf(c.getValue().getCpf())));
        campoNome.textProperty().addListener((obs, antigo, novo) -> atualizar());
        campoCpf.textProperty().addListener((obs, antigo, novo) -> atualizar());
        atualizar();
    }

    private void atualizar() {
        String nome = campoNome.getText() == null ? "" : campoNome.getText().trim().toLowerCase();
        String cpf = campoCpf.getText() == null ? "" : campoCpf.getText().replaceAll("\\D", "");
        tabelaClientes.getItems().setAll(clienteService.listar().stream()
                .filter(cliente -> cliente.getNome().toLowerCase().contains(nome))
                .filter(cliente -> cliente.getCpf().replaceAll("\\D", "").contains(cpf))
                .collect(java.util.stream.Collectors.toList()));
    }

    @FXML
    private void aoRemover() {
        Cliente selecionado = tabelaClientes.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione um cliente para remover.").showAndWait();
            return;
        }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Remover o cliente \"" + selecionado.getNome() + "\"?");
        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            boolean removido = clienteService.remover(selecionado.getId());
            if (!removido) {
                new Alert(Alert.AlertType.WARNING, "O cliente não foi encontrado.").showAndWait();
            }
            atualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
        }
    }
}
