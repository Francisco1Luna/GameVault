package gui;

import model.Plataforma;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import negocio.PlataformaService;
import javafx.scene.control.Alert;

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
        } catch (IllegalArgumentException e) {
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
        try {
            service.remover(linha.getId());
            atualizar();
        } catch (IllegalStateException | IllegalArgumentException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
        }
    }
}