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
import negocio.FreeToGameService;
import javafx.concurrent.Task;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import java.util.LinkedHashMap;
import java.util.Map;

public class JogosController {

    // ---------------------------------------------------------------- campos

    private final JogoService jogoService = Contexto.get().getJogoService();
    private final EstoqueService estoqueService = Contexto.get().getEstoqueService();
    private final PlataformaService plataformaService = Contexto.get().getPlataformaService();
    private final FreeToGameService freeToGameService = new FreeToGameService();

    @FXML private TableView<Jogo> tabelaJogos;
    @FXML private TableColumn<Jogo, Long> colId;
    @FXML private TableColumn<Jogo, String> colTitulo;
    @FXML private TableColumn<Jogo, String> colGenero;
    @FXML private TableColumn<Jogo, Double> colPreco;
    @FXML private TableColumn<Jogo, Double> colLocacao;
    @FXML private TableColumn<Jogo, String> colPlataformas;
    @FXML private TextField campoTitulo;
    @FXML private TextField campoGenero;
    @FXML private TextField campoPreco;
    @FXML private TextField campoLocacao;
    @FXML private TextField campoEstoque;
    @FXML private ComboBox<ClassificacaoIndicativa> comboClassificacao;
    @FXML private ListView<Plataforma> listaPlataformas;
    @FXML private TextField campoPesquisa;
    @FXML private TextField campoDesenvolvedora;
    @FXML private TextField campoPublicadora;
    @FXML private TextField campoLink;
    private String urlThumbnail;
    private boolean importando; // só é mexido na thread da interface

    // ------------------------------------------------------------ initialize

    @FXML
    public void initialize() {
        Formatadores.decimal(campoPreco);
        Formatadores.decimal(campoLocacao);
        Formatadores.inteiro(campoEstoque);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        colLocacao.setCellValueFactory(new PropertyValueFactory<>("precoLocacaoDiaria"));
        colPlataformas.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPlataformas() == null ? "" : c.getValue().getPlataformas().stream()
                        .map(Plataforma::getNome).collect(java.util.stream.Collectors.joining(", "))));

        comboClassificacao.getItems().setAll(ClassificacaoIndicativa.values());
        comboClassificacao.setPromptText("Selecione"); // sem valor padrão: a classificação é obrigatória
        listaPlataformas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listaPlataformas.setItems(FXCollections.observableArrayList(plataformaService.listar()));
        campoPesquisa.textProperty().addListener((obs, antigo, novo) -> atualizar());
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
                campoPreco.setText(textoNumero(novo.getPreco()));
                campoLocacao.setText(textoNumero(novo.getPrecoLocacaoDiaria()));
                campoDesenvolvedora.setText(novo.getDesenvolvedora());
                campoPublicadora.setText(novo.getPublicadora());
                campoLink.setText(novo.getUrlReferencia());
                urlThumbnail = novo.getUrlThumbnail();

                // jogo importado da API ainda não tem classificação: o combo fica vazio e o
                // administrador precisa escolher uma (não vira LIVRE sem ninguém perceber)
                if (novo.getClassificacaoIndicativa() == null) {
                    comboClassificacao.setValue(null);
                } else {
                    comboClassificacao.getSelectionModel().select(novo.getClassificacaoIndicativa());
                }

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

    // --------------------------------------------------------------- handlers

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
            Jogo novo = new Jogo(null, titulo, comboClassificacao.getValue(), campoDesenvolvedora.getText(), genero, campoPublicadora.getText(),
                    preco, locacao, plataformas, new HashMap<>(), urlThumbnail, campoLink.getText());
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
            Alertas.avisar(e.getMessage());
        }
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
            Alertas.avisar(e.getMessage());
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
            // 1. LER os campos
            String titulo = campoTitulo.getText();
            String genero = campoGenero.getText();
            double preco = lerValor(campoPreco, "Preço");
            double locacao = lerValor(campoLocacao, "Locação por dia");
            List<Plataforma> plataformas = new ArrayList<>(listaPlataformas.getSelectionModel().getSelectedItems());
            boolean mudarEstoque = campoEstoque.getText() != null && !campoEstoque.getText().trim().isEmpty();
            int estoque = lerEstoque(); // lança aviso se não for inteiro >= 0

            // 2. o service valida tudo (título, gênero, classificação, preço e locação maiores que zero)

            // 3. CONFIRMAR
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                    "Salvar as alterações em \"" + selecionado.getTitulo() + "\"?");
            if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            // 4. APLICAR: o service valida o jogo novo e só então copia para o guardado; se algo for
            // inválido (preço, classificação, plataforma com locação em andamento...) nada muda
            Jogo novo = new Jogo(selecionado.getId(), titulo, comboClassificacao.getValue(),
                    campoDesenvolvedora.getText(), genero, campoPublicadora.getText(), preco, locacao,
                    plataformas, new HashMap<>(), urlThumbnail, campoLink.getText());
            jogoService.atualizar(novo);

            // se o campo Estoque foi preenchido, vale para todas as plataformas marcadas
            if (mudarEstoque) {
                for (Plataforma p : plataformas) {
                    estoqueService.definirEstoque(selecionado, p, estoque);
                }
            }
            atualizar();
            limparCampos();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.avisar(e.getMessage());
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
            Alertas.avisar(e.getMessage());
        }
    }

    // -------------------------------------------------------------- auxiliares

    private void atualizar() {
        String pesquisa = campoPesquisa.getText() == null ? "" : campoPesquisa.getText().trim().toLowerCase();
        tabelaJogos.getItems().setAll(jogoService.listar().stream()
                .filter(jogo -> jogo.getTitulo().toLowerCase().contains(pesquisa)
                        || (jogo.getGenero() != null && jogo.getGenero().toLowerCase().contains(pesquisa)))
                .collect(java.util.stream.Collectors.toList()));
    }

    private void limparCampos() {
        campoTitulo.clear();
        campoGenero.clear();
        campoPreco.clear();
        campoLocacao.clear();
        campoEstoque.clear();
        campoDesenvolvedora.clear();
        campoPublicadora.clear();
        campoLink.clear();
        urlThumbnail = null;
        comboClassificacao.setValue(null);
        listaPlataformas.getSelectionModel().clearSelection();
    }

    /** Número para o campo de texto: sempre em notação simples (nunca 1.0E7), com duas casas. */
    private static String textoNumero(Double valor) {
        return valor == null ? "" : String.format(java.util.Locale.ROOT, "%.2f", valor);
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

    @FXML
    private void aoBuscarDados() {
        String titulo = campoTitulo.getText();
        Task<FreeToGameService.DadosJogo> consulta = new Task<>() {
            @Override protected FreeToGameService.DadosJogo call() { return freeToGameService.buscarPorTitulo(titulo); }
        };
        consulta.setOnSucceeded(evento -> preencherDadosApi(consulta.getValue()));
        consulta.setOnFailed(evento -> Alertas.avisar(consulta.getException().getMessage()));
        Thread thread = new Thread(consulta, "consulta-freetogame");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void aoImportarCatalogo() {
        if (importando) {
            return; // já tem uma importação em andamento
        }
        importando = true;
        Task<Integer> importacao = new Task<>() {
            // o service faz tudo ou nada e sob um único bloqueio (não duplica plataformas)
            @Override protected Integer call() { return jogoService.importarDaApi(freeToGameService.listarJogos()); }
        };
        importacao.setOnSucceeded(evento -> {
            importando = false;
            listaPlataformas.setItems(FXCollections.observableArrayList(plataformaService.listar()));
            atualizar();
            Alertas.informar(importacao.getValue() + " jogos novos foram importados da FreeToGame.\n"
                    + "Eles entram sem preço, locação, classificação e estoque: complete o cadastro de cada um "
                    + "(Salvar alterações e Ajustar estoque) para liberar a compra e o aluguel na loja.");
        });
        importacao.setOnFailed(evento -> {
            importando = false;
            Alertas.avisar(importacao.getException().getMessage());
        });
        Thread thread = new Thread(importacao, "importacao-freetogame");
        thread.setDaemon(true);
        thread.start();
    }

    private void preencherDadosApi(FreeToGameService.DadosJogo dados) {
        campoTitulo.setText(dados.getTitulo());
        campoGenero.setText(dados.getGenero());
        campoDesenvolvedora.setText(dados.getDesenvolvedora());
        campoPublicadora.setText(dados.getPublicadora());
        campoLink.setText(dados.getUrlReferencia());
        urlThumbnail = dados.getUrlThumbnail();
        listaPlataformas.getSelectionModel().clearSelection();
        String plataformasApi = dados.getPlataformas() == null ? "" : dados.getPlataformas().toLowerCase();
        for (Plataforma plataforma : listaPlataformas.getItems()) {
            String nome = plataforma.getNome().toLowerCase();
            if ((nome.contains("pc") && plataformasApi.contains("pc")) || (nome.contains("web") && plataformasApi.contains("web"))) {
                listaPlataformas.getSelectionModel().select(plataforma);
            }
        }
    }
}
