package gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Cliente;
import model.ClienteAssinante;
import negocio.AuthService;
import negocio.ClienteService;

public class ClientesController {

    private final ClienteService clienteService = Contexto.get().getClienteService();
    private final AuthService authService = Contexto.get().getAuthService();

    @FXML private TableView<Cliente> tabelaClientes;
    @FXML private TableColumn<Cliente, Long> colId;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colCpf;
    @FXML private TableColumn<Cliente, String> colTipo;
    @FXML private TextField campoNome;
    @FXML private TextField campoCpf;
    @FXML private DatePicker campoNascimento;
    @FXML private TextField campoEmail;
    @FXML private TextField campoTelefone;
    @FXML private PasswordField campoSenha;
    @FXML private CheckBox checkAssinante;
    @FXML private TextField campoPesquisa;

    @FXML
    public void initialize() {
        Formatadores.cpf(campoCpf);
        Formatadores.telefone(campoTelefone);
        Formatadores.data(campoNascimento);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCpf.setCellValueFactory(c -> new SimpleStringProperty(Formatadores.formatarCpf(c.getValue().getCpf())));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue() instanceof ClienteAssinante ? "Assinante" : "Padrão"));
        campoPesquisa.textProperty().addListener((obs, antigo, novo) -> atualizar());

        // ao clicar numa linha, preenche os campos para editar
        tabelaClientes.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                campoNome.setText(novo.getNome());
                campoCpf.setText(novo.getCpf());
                campoNascimento.setValue(novo.getDataNascimento());
                campoEmail.setText(novo.getEmail());
                campoTelefone.setText(novo.getTelefone());
                campoSenha.clear();
                checkAssinante.setSelected(novo instanceof ClienteAssinante);
            }
        });
        atualizar();
    }

    private void atualizar() {
        String pesquisa = campoPesquisa.getText() == null ? "" : campoPesquisa.getText().trim().toLowerCase();
        tabelaClientes.getItems().setAll(clienteService.listar().stream()
                .filter(cliente -> cliente.getNome().toLowerCase().contains(pesquisa)
                        || cliente.getCpf().contains(pesquisa.replaceAll("\\D", "")))
                .collect(java.util.stream.Collectors.toList()));
    }

    @FXML
    private void aoAdicionar() {
        try {
            // o AuthService valida tudo, gera o hash da senha e chama o ClienteService.salvar
            authService.cadastrar(
                    campoNome.getText(), campoCpf.getText(), campoNascimento.getValue(),
                    campoEmail.getText(), campoTelefone.getText(), campoSenha.getText(),
                    checkAssinante.isSelected());
            limparCampos();
            atualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.avisar(e.getMessage());
        }
    }

    @FXML
    private void aoEditar() {
        Cliente selecionado = tabelaClientes.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione um cliente para editar.").showAndWait();
            return;
        }
        try {
            // 1. CONFIRMAR

            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                    "Salvar as alterações em \"" + selecionado.getNome() + "\"?");
            if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            // 2. APLICAR: o service valida tudo antes de alterar (dados, tipo Padrão/Assinante e, se o
            // campo senha estiver preenchido, a nova senha); se algo for inválido nada muda
            authService.editarCliente(selecionado.getId(), campoNome.getText(), campoCpf.getText(),
                    campoNascimento.getValue(), campoEmail.getText(), campoTelefone.getText(),
                    checkAssinante.isSelected(), campoSenha.getText());

            limparCampos();
            atualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.avisar(e.getMessage());
        }
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
            Alertas.avisar(e.getMessage());
        }
    }

    private void limparCampos() {
        campoNome.clear();
        campoCpf.clear();
        campoNascimento.setValue(null);
        campoEmail.clear();
        campoTelefone.clear();
        campoSenha.clear();
        checkAssinante.setSelected(false);
    }

}
