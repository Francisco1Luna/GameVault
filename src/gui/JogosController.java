package gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Jogo;
import model.Plataforma;
import model.enums.ClassificacaoIndicativa;
import negocio.EstoqueService;
import negocio.PlataformaService;
import negocio.JogoService;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import java.util.LinkedHashMap;
import java.util.Map;

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
        tabelaJogos.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                campoTitulo.setText(novo.getTitulo());
                campoGenero.setText(novo.getGenero());
                campoPreco.setText(String.valueOf(novo.getPreco()));
                campoLocacao.setText(String.valueOf(novo.getPrecoLocacaoDiaria()));

                // classificação: se o jogo não tiver, volta para LIVRE (select(null) só limparia o combo)
                comboClassificacao.getSelectionModel().select(
                        novo.getClassificacaoIndicativa() == null
                                ? ClassificacaoIndicativa.LIVRE
                                : novo.getClassificacaoIndicativa());

                // marca na lista as plataformas do jogo selecionado (comparando por id)
                listaPlataformas.getSelectionModel().clearSelection();
                if (novo.getPlataformas() != null) {
                    for (Plataforma item : listaPlataformas.getItems()) {
                        for (Plataforma doJogo : novo.getPlataformas()) {
                            if (item.getId() != null && item.getId().equals(doJogo.getId())) {
                                listaPlataformas.getSelectionModel().select(item);
                            }
                        }
                    }
                }
            }
        });
        atualizar();
    }

    @FXML
    private void aoAdicionar() {
        try {
            // 1. LER
            String titulo = campoTitulo.getText();
            String genero = campoGenero.getText();
            double preco = lerValor(campoPreco, "Preço");
            double locacao = lerValor(campoLocacao, "Locação por dia");
            int estoque = lerEstoque();
            List<Plataforma> plataformas = new ArrayList<>(listaPlataformas.getSelectionModel().getSelectedItems());

            // 2. VALIDAR (o título também é validado pelo service)
            if (genero == null || genero.trim().isEmpty()) {
                throw new IllegalArgumentException("O gênero não pode ficar vazio.");
            }

            // 3. SALVAR
            Jogo novo = new Jogo(null, titulo, comboClassificacao.getValue(), null, genero, null,
                    preco, locacao, plataformas, new HashMap<>(), null, null);
            Jogo salvo = jogoService.salvar(novo);
            for (Plataforma plataforma : plataformas) {
                estoqueService.definirEstoque(salvo, plataforma, estoque); // mesmo estoque em cada plataforma marcada
            }
            limparCampos();
            atualizar();
            if (plataformas.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "Jogo adicionado sem plataforma. Ele aparece na Loja, mas só pode ser "
                        + "comprado ou alugado depois de ter plataforma e estoque.").showAndWait();
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
        }
    }

    /**
     * Lê um campo numérico não negativo. Aceita vírgula ou ponto como separador decimal.
     * Qualquer problema vira IllegalArgumentException com mensagem amigável.
     */
    private double lerValor(TextField campo, String nomeDoCampo) {
        String texto = campo.getText() == null ? "" : campo.getText().trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("O campo " + nomeDoCampo + " é obrigatório.");
        }
        double valor;
        try {
            valor = Double.parseDouble(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O campo " + nomeDoCampo + " deve ser um número.");
        }
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            throw new IllegalArgumentException("O campo " + nomeDoCampo + " deve ser um número.");
        }
        if (valor < 0) {
            throw new IllegalArgumentException("O campo " + nomeDoCampo + " não pode ser negativo.");
        }
        return valor;
    }

    /**
     * Estoque vazio vale 0; texto que não é número inteiro não negativo vira aviso.
     */
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

    private void limparCampos() {
        campoTitulo.clear();
        campoGenero.clear();
        campoPreco.clear();
        campoLocacao.clear();
        campoEstoque.clear();
        listaPlataformas.getSelectionModel().clearSelection();
    }

    @FXML
    private void aoRemover() {
        Jogo selecionado = tabelaJogos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione um jogo para remover.").showAndWait();
            return;
        }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION, "Remover o jogo \"" + selecionado.getTitulo() + "\"?");
        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            boolean removido = jogoService.remover(selecionado.getId());
            if (!removido) {
                new Alert(Alert.AlertType.WARNING, "O jogo nao foi encontrado.").showAndWait();
            }
            atualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void aoEditar() {
        Jogo selecionado = tabelaJogos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione um jogo para editar.").showAndWait();
            return;
        }
        try {
            // 1. LER tudo (sem alterar o objeto)
            String titulo = campoTitulo.getText();
            String genero = campoGenero.getText();
            double preco = lerValor(campoPreco, "Preço");
            double locacao = lerValor(campoLocacao, "Locação por dia");
            List<Plataforma> plataformas = new ArrayList<>(listaPlataformas.getSelectionModel().getSelectedItems());
            boolean mudarEstoque = campoEstoque.getText() != null && !campoEstoque.getText().trim().isEmpty();
            int estoque = lerEstoque(); // lança aviso se não for inteiro >= 0

            // 2. VALIDAR tudo antes de qualquer set
            if (titulo == null || titulo.trim().isEmpty()) {
                throw new IllegalArgumentException("O título não pode ficar vazio.");
            }
            if (genero == null || genero.trim().isEmpty()) {
                throw new IllegalArgumentException("O gênero não pode ficar vazio.");
            }

            // 3. CONFIRMAR (só depois de saber que os dados são válidos)
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                    "Salvar as alterações em \"" + selecionado.getTitulo() + "\"?");
            if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            // 4. APLICAR
            selecionado.setTitulo(titulo);
            selecionado.setGenero(genero);
            selecionado.setPreco(preco);
            selecionado.setPrecoLocacaoDiaria(locacao);
            selecionado.setClassificacaoIndicativa(comboClassificacao.getValue());
            selecionado.setPlataformas(plataformas);

            // remove do mapa o estoque de plataformas que foram desmarcadas
            if (selecionado.getEstoquePorPlataforma() != null) {
                selecionado.getEstoquePorPlataforma().keySet().removeIf(chave ->
                        plataformas.stream().noneMatch(p -> p.getId() != null && p.getId().equals(chave.getId())));
            }
            // se o campo Estoque foi preenchido, vale para todas as plataformas marcadas
            if (mudarEstoque) {
                for (Plataforma p : plataformas) {
                    estoqueService.definirEstoque(selecionado, p, estoque);
                }
            }

            jogoService.atualizar(selecionado);
            atualizar();
            limparCampos();
        } catch (IllegalArgumentException | IllegalStateException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void aoAjustarEstoque() {
        Jogo selecionado = tabelaJogos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione um jogo para ajustar o estoque.").showAndWait();
            return;
        }
        List<Plataforma> plataformas = selecionado.getPlataformas();
        if (plataformas == null || plataformas.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Este jogo não tem plataforma. Marque as plataformas e "
                    + "clique em Salvar alterações primeiro.").showAndWait();
            return;
        }
        try {
            // monta a janela com o estoque atual de cada plataforma
            Dialog<ButtonType> dialogo = new Dialog<>();
            dialogo.setTitle("Ajustar estoque");
            dialogo.setHeaderText("Estoque de \"" + selecionado.getTitulo() + "\" por plataforma");
            dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane grade = new GridPane();
            grade.setHgap(10);
            grade.setVgap(8);
            grade.setPadding(new Insets(10));
            Map<Plataforma, TextField> campos = new LinkedHashMap<>();
            int linha = 0;
            for (Plataforma plataforma : plataformas) {
                TextField campo = new TextField(
                        String.valueOf(estoqueService.consultarQuantidade(selecionado, plataforma)));
                grade.add(new Label(plataforma.getNome()), 0, linha);
                grade.add(campo, 1, linha);
                campos.put(plataforma, campo);
                linha++;
            }
            dialogo.getDialogPane().setContent(grade);
            if (dialogo.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            // 1. LER e VALIDAR todos os campos antes de aplicar qualquer estoque
            Map<Plataforma, Integer> novos = new LinkedHashMap<>();
            for (Map.Entry<Plataforma, TextField> par : campos.entrySet()) {
                novos.put(par.getKey(), lerQuantidade(par.getValue(), par.getKey().getNome()));
            }

            // 2. APLICAR
            for (Map.Entry<Plataforma, Integer> par : novos.entrySet()) {
                estoqueService.definirEstoque(selecionado, par.getKey(), par.getValue());
            }
            atualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
        }
    }

    /** Lê o estoque de uma plataforma: inteiro, não negativo, com a plataforma citada na mensagem de erro. */
    private int lerQuantidade(TextField campo, String nomePlataforma) {
        String texto = campo.getText() == null ? "" : campo.getText().trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Informe o estoque de " + nomePlataforma + ".");
        }
        try {
            int quantidade = Integer.parseInt(texto);
            if (quantidade < 0) {
                throw new IllegalArgumentException("O estoque de " + nomePlataforma + " não pode ser negativo.");
            }
            return quantidade;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O estoque de " + nomePlataforma + " deve ser um número inteiro.");
        }
    }
}