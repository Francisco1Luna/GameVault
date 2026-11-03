package gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import model.Jogo;
import model.Plataforma;
import model.enums.ClassificacaoIndicativa;
import negocio.EstoqueService;
import negocio.PlataformaService;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import negocio.JogoService;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.scene.control.Alert;

public class JogosController {

    private final JogoService jogoService = Contexto.get().getJogoService();
    private final EstoqueService estoqueService = Contexto.get().getEstoqueService();
    private final PlataformaService plataformaService = Contexto.get().getPlataformaService();

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
    private TableColumn<Jogo, String> colPlataformas;

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
    private ComboBox<ClassificacaoIndicativa> comboClassificacao;

    @FXML
    private ListView<Plataforma> listaPlataformas;

    @FXML
    private TextField campoEstoque;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        colLocacao.setCellValueFactory(new PropertyValueFactory<>("precoLocacaoDiaria"));
        colPlataformas.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPlataformas() == null ? "" : c.getValue().getPlataformas().stream()
                        .map(Plataforma::getNome).collect(java.util.stream.Collectors.joining(", "))));

        comboClassificacao.getItems().setAll(ClassificacaoIndicativa.values());
        comboClassificacao.getSelectionModel().select(ClassificacaoIndicativa.LIVRE);
        listaPlataformas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listaPlataformas.setItems(FXCollections.observableArrayList(plataformaService.listar()));
        listaPlataformas.setCellFactory(lista -> new ListCell<Plataforma>() {
            @Override
            protected void updateItem(Plataforma plataforma, boolean vazio) {
                super.updateItem(plataforma, vazio);
                setText(vazio || plataforma == null ? null : plataforma.getNome());
            }
        });
        atualizar();
    }

    @FXML
    private void aoAdicionar() {
        try {
            String titulo = campoTitulo.getText();
            String genero = campoGenero.getText();
            Double preco = Double.parseDouble(campoPreco.getText().trim().replace(',', '.'));
            Double locacao = Double.parseDouble(campoLocacao.getText().trim().replace(',', '.'));
            int estoque = lerEstoque();
            List<Plataforma> plataformas = new ArrayList<>(listaPlataformas.getSelectionModel().getSelectedItems());
            Jogo novo = new Jogo(null, titulo, comboClassificacao.getValue(), null, genero, null,
                    preco, locacao, plataformas, new HashMap<>(), null, null);
            Jogo salvo = jogoService.salvar(novo);
            for (Plataforma plataforma : plataformas) {
                estoqueService.definirEstoque(salvo, plataforma, estoque); // mesmo estoque em cada plataforma marcada
            }
            campoTitulo.clear();
            campoGenero.clear();
            campoPreco.clear();
            campoLocacao.clear();
            campoEstoque.clear();
            listaPlataformas.getSelectionModel().clearSelection();
            atualizar();
            if (plataformas.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "Jogo adicionado sem plataforma. Ele aparece na Loja, mas só pode ser "
                        + "comprado ou alugado depois de ter plataforma e estoque.").showAndWait();
            }
        } catch (IllegalArgumentException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
        }

    }

    /** Estoque vazio vale 0; texto que não é número inteiro não negativo vira aviso. */
    private int lerEstoque() {
        String texto = campoEstoque.getText() == null ? "" : campoEstoque.getText().trim();
        if (texto.isEmpty()) {
            return 0;
        }
        try {
            int quantidade = Integer.parseInt(texto);
            if (quantidade < 0) {
                throw new IllegalArgumentException("O estoque não pode ser negativo.");
            }
            return quantidade;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O estoque deve ser um número inteiro.");
        }
    }

    @FXML
    private void aoRemover() {

    }
}
