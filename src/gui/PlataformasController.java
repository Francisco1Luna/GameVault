package gui;

import javafx.scene.control.*;
import model.Plataforma;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;
import negocio.PlataformaService;

public class PlataformasController {

    private final PlataformaService service = Contexto.get().getPlataformaService();
    @FXML
    private TextField campoNome;

    @FXML
    private TextField campoFabricante;

    @FXML
    private TableView<Plataforma> tabelaPlataformas;

    @FXML
    private TableColumn<Plataforma, Long> colId;

    @FXML
    private TableColumn<Plataforma, String> colNome;

    @FXML
    private TableColumn<Plataforma, String> colFabricante;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colFabricante.setCellValueFactory(new PropertyValueFactory<>("fabricante"));

        // ao clicar numa linha, preenche os campos para editar
        tabelaPlataformas.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                campoNome.setText(novo.getNome());
                campoFabricante.setText(novo.getFabricante());
            }
        });
        atualizar();
    }

    private void atualizar() {
        tabelaPlataformas.getItems().setAll(service.listar());
    }

    @FXML
    private void aoAdicionar() {
        try {
            String fabricante = campoFabricante.getText();
            String nome = campoNome.getText();
            Plataforma nova = new Plataforma(null, nome, fabricante);
            service.cadastrar(nova);
            campoNome.clear();
            campoFabricante.clear();
            atualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void aoEditar() {
        Plataforma selecionada = tabelaPlataformas.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione uma plataforma para editar.").showAndWait();
            return;
        }
        try {
            // 1. LER (sem alterar o objeto)
            String nome = campoNome.getText();
            String fabricante = campoFabricante.getText();

            // 2. VALIDAR antes de qualquer set (o objeto selecionado é o mesmo guardado no repositório)
            if (nome == null || nome.trim().isEmpty()) {
                throw new IllegalArgumentException("O nome da plataforma não pode ficar vazio.");
            }
            if (fabricante == null || fabricante.trim().isEmpty()) {
                throw new IllegalArgumentException("O fabricante não pode ficar vazio.");
            }

            // 3. CONFIRMAR
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                    "Salvar as alterações em \"" + selecionada.getNome() + "\"?");
            if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            // 4. APLICAR no MESMO objeto: os jogos que usam esta plataforma enxergam o novo nome
            selecionada.setNome(nome.trim());
            selecionada.setFabricante(fabricante.trim());
            service.atualizar(selecionada);

            campoNome.clear();
            campoFabricante.clear();
            atualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void aoRemover() {
        Plataforma linha = tabelaPlataformas.getSelectionModel().getSelectedItem();
        if (linha == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecione uma plataforma.").showAndWait();
            return;
        }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION, "Remover a plataforma \"" + linha.getNome() + "\"?");
        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            if (!service.remover(linha.getId())) {
                new Alert(Alert.AlertType.WARNING, "A plataforma nao foi encontrada.").showAndWait();
            }
            atualizar();
        } catch (IllegalStateException | IllegalArgumentException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
        }
    }
}