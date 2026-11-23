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
    @FXML private TextField campoPesquisa;

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
        campoPesquisa.textProperty().addListener((obs, antigo, novo) -> atualizar());

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
        String pesquisa = campoPesquisa.getText() == null ? "" : campoPesquisa.getText().trim().toLowerCase();
        tabelaPlataformas.getItems().setAll(service.listar().stream()
                .filter(plataforma -> contem(plataforma.getNome(), pesquisa)
                        || contem(plataforma.getFabricante(), pesquisa))
                .collect(java.util.stream.Collectors.toList()));
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
            Alertas.avisar(e.getMessage());        }
    }

    @FXML
    private void aoEditar() {
        Plataforma selecionada = tabelaPlataformas.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            Alertas.avisar("Selecione uma plataforma para editar.");
            return;
        }
        try {
            // 1. LER (sem alterar o objeto)
            String nome = campoNome.getText();
            String fabricante = campoFabricante.getText();

            // 2. CONFIRMAR (quem valida é o service)
            if (!Alertas.confirmar("Salvar alterações", "Salvar as alterações em \"" + selecionada.getNome() + "\"?")) {
                return;
            }

            // 3. APLICAR: o service valida o objeto novo e só então copia para a plataforma guardada
            // (a mesma que os jogos referenciam, então eles enxergam o novo nome)
            service.atualizar(new Plataforma(selecionada.getId(), nome, fabricante));

            campoNome.clear();
            campoFabricante.clear();
            atualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.avisar(e.getMessage());        }
    }

    @FXML
    private void aoRemover() {
        Plataforma linha = tabelaPlataformas.getSelectionModel().getSelectedItem();
        if (linha == null) {
            Alertas.avisar("Selecione uma plataforma para remover.");
            return;
        }
        if (!Alertas.confirmar("Remover plataforma", "Remover a plataforma \"" + linha.getNome() + "\"?")) {
            return;
        }
        try {
            if (!service.remover(linha.getId())) {
                Alertas.avisar("A plataforma não foi encontrada.");
            }
            atualizar();
        } catch (IllegalStateException | IllegalArgumentException e) {
            Alertas.avisar(e.getMessage());        }
    }

    /** Pesquisa sem quebrar quando o campo é nulo (ex.: plataforma sem fabricante). */
    private static boolean contem(String texto, String pesquisa) {
        return texto != null && texto.toLowerCase().contains(pesquisa);
    }
}
