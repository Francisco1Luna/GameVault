package gui;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import model.Jogo;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import negocio.JogoService;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.control.Alert;

public class JogosController {

    private final JogoService jogoService = Contexto.get().getJogoService();

    private void atualizar() {
        tabelaJogos.getItems().setAll(jogoService.listar());
    }

    @FXML
    private TableColumn<Jogo, Long> colId;

    @FXML
    private TableColumn<Jogo, String> colTitulo;

    @FXML
    private TableColumn<Jogo, String> colGenero;

    @FXML
    private TableColumn<Jogo, Double> colPreco;

    @FXML
    private TableColumn<Jogo, Double> colLocacao;

    @FXML
    private TableView<Jogo> tabelaJogos;

    @FXML
    private TextField campoTitulo;

    @FXML
    private TextField campoGenero;

    @FXML
    private TextField campoPreco;

    @FXML
    private TextField campoLocacao;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        colLocacao.setCellValueFactory(new PropertyValueFactory<>("precoLocacaoDiaria"));
        atualizar();
    }

    @FXML
    private void aoAdicionar() {
        try {
            String titulo = campoTitulo.getText();
            String genero = campoGenero.getText();
            Double preco = Double.parseDouble(campoPreco.getText().trim().replace(',', '.'));
            Double locacao = Double.parseDouble(campoLocacao.getText().trim().replace(',', '.'));
            Jogo novo = new Jogo(null, titulo, null, null, genero, null,
                    preco, locacao, new ArrayList<>(), new HashMap<>(), null, null);
            jogoService.salvar(novo);
            campoTitulo.clear();
            campoGenero.clear();
            campoPreco.clear();
            campoLocacao.clear();
            atualizar();
        } catch (IllegalArgumentException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
        }

    }

    @FXML
    private void aoRemover() {

    }
}
