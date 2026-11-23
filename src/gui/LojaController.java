package gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.Aluguel;
import model.Cliente;
import model.ClienteAssinante;
import model.ItemVenda;
import model.Jogo;
import model.Plataforma;
import model.Venda;
import model.enums.StatusAluguel;
import negocio.AuthService;
import negocio.DevolucaoService;
import negocio.EstoqueService;
import negocio.FinanceiroService;
import negocio.JogoService;
import negocio.AluguelService;
import negocio.VendaService;
import negocio.excecoes.ValidacaoException;
import negocio.Relogio;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Loja do usuário logado: catálogo com o preço dele (com desconto, se for assinante), compra,
 * locação, histórico e devolução. Quem compra/aluga é sempre o usuário atual (AuthService).
 */
public class LojaController {

    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AuthService auth = Contexto.get().getAuthService();
    private final JogoService jogoService = Contexto.get().getJogoService();
    private final EstoqueService estoqueService = Contexto.get().getEstoqueService();
    private final FinanceiroService financeiroService = Contexto.get().getFinanceiroService();
    private final VendaService vendaService = Contexto.get().getVendaService();
    private final AluguelService aluguelService = Contexto.get().getAluguelService();
    private final DevolucaoService devolucaoService = Contexto.get().getDevolucaoService();

    @FXML private Label rotuloConta;
    @FXML private TextField campoPesquisaCatalogo;
    @FXML private TableView<ItemCatalogo> tabelaCatalogo;
    @FXML private TableColumn<ItemCatalogo, String> colJogo;
    @FXML private TableColumn<ItemCatalogo, String> colPlataforma;
    @FXML private TableColumn<ItemCatalogo, String> colPreco;
    @FXML private TableColumn<ItemCatalogo, String> colDiaria;
    @FXML private TableColumn<ItemCatalogo, String> colEstoque;
    @FXML private Spinner<Integer> spinnerQuantidade;
    @FXML private Spinner<Integer> spinnerDias;
    @FXML private Label rotuloTotalCompra;
    @FXML private Label rotuloTotalLocacao;
    @FXML private Button botaoComprar;
    @FXML private Button botaoAlugar;

    @FXML private TableView<Venda> tabelaCompras;
    @FXML private TableColumn<Venda, String> colCompraData;
    @FXML private TableColumn<Venda, String> colCompraItens;
    @FXML private TableColumn<Venda, String> colCompraTotal;

    @FXML private TableView<Aluguel> tabelaLocacoes;
    @FXML private TableColumn<Aluguel, String> colLocJogo;
    @FXML private TableColumn<Aluguel, String> colLocPlataforma;
    @FXML private TableColumn<Aluguel, String> colLocInicio;
    @FXML private TableColumn<Aluguel, String> colLocPrazo;
    @FXML private TableColumn<Aluguel, String> colLocStatus;
    @FXML private TableColumn<Aluguel, String> colLocValor;
    @FXML private TableColumn<Aluguel, String> colLocMulta;
    @FXML private Button botaoDevolver;

    @FXML
    public void initialize() {
        spinnerQuantidade.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
        spinnerDias.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 1));
        campoPesquisaCatalogo.textProperty().addListener((obs, antigo, novo) -> atualizarTudo());

        colJogo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().jogo.getTitulo()));
        colPlataforma.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().plataforma == null ? "(sem plataforma)" : c.getValue().plataforma.getNome()));
        colPreco.setCellValueFactory(c -> new SimpleStringProperty(textoPreco(c.getValue().jogo)));
        colDiaria.setCellValueFactory(c -> new SimpleStringProperty(dinheiro(c.getValue().jogo.getPrecoLocacaoDiaria())));
        colEstoque.setCellValueFactory(c -> new SimpleStringProperty(textoEstoque(c.getValue())));
        // colunas de texto formatado: ordenar pelo valor, não alfabeticamente (ver Ordenacao)
        colPreco.setComparator(Ordenacao.DINHEIRO);
        colDiaria.setComparator(Ordenacao.DINHEIRO);
        colEstoque.setComparator(Ordenacao.NUMERO);

        colCompraData.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataVenda().format(DATA)));
        colCompraItens.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getItens().stream()
                .map(i -> i.getJogo().getTitulo() + " (" + i.getPlataforma().getNome() + ") x" + i.getQuantidade())
                .collect(Collectors.joining(", "))));
        colCompraTotal.setCellValueFactory(c -> new SimpleStringProperty(dinheiro(c.getValue().getValorTotal())));
        colCompraData.setComparator(Ordenacao.DATA_BR);
        colCompraTotal.setComparator(Ordenacao.DINHEIRO);

        colLocJogo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJogo().getTitulo()));
        colLocPlataforma.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPlataforma().getNome()));
        colLocInicio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataInicio().format(DATA)));
        colLocPrazo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataDevolucaoPrevista().format(DATA)));
        // o status ATRASADO é gravado pelo AluguelService.atualizarAtrasos() (chamado em atualizarTudo)
        // status em português e como etiqueta colorida (antes aparecia o nome do enum: ATIVO, ATRASADO...)
        colLocStatus.setCellValueFactory(c -> new SimpleStringProperty(textoStatus(c.getValue().getStatus())));
        colLocStatus.setCellFactory(coluna -> new CelulaEtiqueta());
        colLocValor.setCellValueFactory(c -> new SimpleStringProperty(dinheiro(c.getValue().getValorLocacao())));
        colLocMulta.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPenalidade() == null
                ? "-" : dinheiro(c.getValue().getPenalidade().getValor())));
        colLocInicio.setComparator(Ordenacao.DATA_BR);
        colLocPrazo.setComparator(Ordenacao.DATA_BR);
        colLocValor.setComparator(Ordenacao.DINHEIRO);
        colLocMulta.setComparator(Ordenacao.DINHEIRO);

        // só dá para comprar/alugar uma linha marcada que tenha plataforma
        // linha que não pode ser comprada/alugada (sem plataforma, cadastro incompleto ou sem estoque) fica apagada
        tabelaCatalogo.setRowFactory(tabela -> new javafx.scene.control.TableRow<ItemCatalogo>() {
            @Override
            protected void updateItem(ItemCatalogo item, boolean vazio) {
                super.updateItem(item, vazio);
                getStyleClass().remove("linha-indisponivel");
                if (!vazio && item != null && !disponivel(item)) {
                    getStyleClass().add("linha-indisponivel");
                }
            }
        });
        botaoComprar.disableProperty().bind(Bindings.createBooleanBinding(this::semPlataformaMarcada,
                tabelaCatalogo.getSelectionModel().selectedItemProperty()));
        botaoAlugar.disableProperty().bind(botaoComprar.disableProperty());
        // só dá para devolver uma locação que ainda está em andamento
        botaoDevolver.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            Aluguel marcado = tabelaLocacoes.getSelectionModel().getSelectedItem();
            return marcado == null || marcado.getStatus() == StatusAluguel.DEVOLVIDO;
        }, tabelaLocacoes.getSelectionModel().selectedItemProperty()));
        tabelaCatalogo.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> atualizarTotais());
        spinnerQuantidade.valueProperty().addListener((o, a, b) -> atualizarTotais());
        spinnerDias.valueProperty().addListener((o, a, b) -> atualizarTotais());

        Cliente usuario = auth.getUsuarioAtual().orElse(null);
        if (usuario == null) {
            rotuloConta.setText("Faça login para comprar ou alugar.");
        } else if (usuario instanceof ClienteAssinante) {
            rotuloConta.setText("Preços para " + usuario.getNome() + " - Assinante ("
                    + String.format(BR, "%.0f", ((ClienteAssinante) usuario).getPercentualDesconto())
                    + "% de desconto nas compras, até " + usuario.getLimiteAluguelSimultaneo() + " locações ao mesmo tempo)");
        } else {
            rotuloConta.setText("Preços para " + usuario.getNome() + " - Cliente comum (até "
                    + usuario.getLimiteAluguelSimultaneo() + " locação ao mesmo tempo)");
        }
        atualizarTudo();
    }

    private boolean semPlataformaMarcada() {
        ItemCatalogo item = tabelaCatalogo.getSelectionModel().getSelectedItem();
        // jogo importado e ainda incompleto (sem preço/classificação) também não pode ser comprado nem alugado
        return item == null || item.plataforma == null || !jogoService.estaCompleto(item.jogo);
    }

    /** A linha pode ser comprada/alugada agora? Nunca lança (é usada ao desenhar a tabela). */
    private boolean disponivel(ItemCatalogo item) {
        try {
            return item.plataforma != null && jogoService.estaCompleto(item.jogo)
                    && estoqueService.consultarQuantidade(item.jogo, item.plataforma) > 0;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Estoque da linha. Nunca lança dentro da célula da tabela: qualquer problema vira "-". */
    private String textoEstoque(ItemCatalogo item) {
        if (item.plataforma == null) {
            return "-";
        }
        try {
            return String.valueOf(estoqueService.consultarQuantidade(item.jogo, item.plataforma));
        } catch (RuntimeException e) {
            return "-";
        }
    }

    // ---- ações ----

    @FXML
    private void aoAtualizar() {
        atualizarTudo();
    }

    @FXML
    private void aoComprar() {
        ItemCatalogo item = tabelaCatalogo.getSelectionModel().getSelectedItem();
        if (item == null) {
            return;
        }
        try {
            int quantidade = spinnerQuantidade.getValue();
            double total = financeiroService.calcularPrecoVenda(item.jogo, auth.exigirUsuarioAtual()) * quantidade;
            if (!confirmar("Confirmar compra?", item.jogo.getTitulo() + " (" + item.plataforma.getNome()
                    + ") x" + quantidade + "\nTotal: " + dinheiro(total))) {
                return;
            }
            ItemVenda pedido = new ItemVenda(item.jogo, item.plataforma, quantidade, 0.0);
            Venda venda = vendaService.registrarVenda(List.of(pedido), Relogio.hoje());
            informar("Compra realizada: " + item.jogo.getTitulo() + " (" + item.plataforma.getNome() + ") x"
                    + quantidade + "\nTotal: " + dinheiro(venda.getValorTotal()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            avisar(e.getMessage());
        }
        atualizarTudo();
    }

    @FXML
    private void aoAlugar() {
        ItemCatalogo item = tabelaCatalogo.getSelectionModel().getSelectedItem();
        if (item == null) {
            return;
        }
        try {
            LocalDate hoje = Relogio.hoje();
            LocalDate prazo = hoje.plusDays(spinnerDias.getValue());
            double valor = financeiroService.calcularValorLocacao(item.jogo, hoje, prazo);
            if (!confirmar("Confirmar aluguel?", item.jogo.getTitulo() + " (" + item.plataforma.getNome()
                    + ")\nDevolver até " + prazo.format(DATA) + "\nValor: " + dinheiro(valor))) {
                return;
            }
            Aluguel aluguel = aluguelService.realizarLocacao(item.jogo, item.plataforma, hoje, prazo);
            informar("Locação feita: " + item.jogo.getTitulo() + " (" + item.plataforma.getNome() + ")\nDevolver até "
                    + aluguel.getDataDevolucaoPrevista().format(DATA) + "\nValor: " + dinheiro(aluguel.getValorLocacao()));
        } catch (ValidacaoException | IllegalArgumentException | IllegalStateException e) {
            avisar(e.getMessage());
        }
        atualizarTudo();
    }

    @FXML
    private void aoDevolver() {
        Aluguel aluguel = tabelaLocacoes.getSelectionModel().getSelectedItem();
        if (aluguel == null) {
            return;
        }
        if (!confirmar("Confirmar devolução?", aluguel.getJogo().getTitulo() + " ("
                + aluguel.getPlataforma().getNome() + ")")) {
            return;
        }
        try {
            // se a data do sistema foi voltada ("Data real") para antes da retirada, devolve no próprio
            // dia da retirada; senão o service recusa ("a devolução não pode ser anterior ao início")
            LocalDate dataDevolucao = aluguel.getDataInicio() != null && Relogio.hoje().isBefore(aluguel.getDataInicio())
                    ? aluguel.getDataInicio() : Relogio.hoje();
            devolucaoService.devolver(aluguel, dataDevolucao);
            String multa = aluguel.getPenalidade() == null ? "" : "\nMulta por atraso: " + dinheiro(aluguel.getPenalidade().getValor());
            informar("Devolução registrada: " + aluguel.getJogo().getTitulo() + multa);
        } catch (ValidacaoException | IllegalArgumentException | IllegalStateException e) {
            avisar(e.getMessage());
        }
        atualizarTudo();
    }

    // ---- atualização das tabelas ----

    private void atualizarTudo() {
        aluguelService.atualizarAtrasos(); // grava ATRASADO nas locações vencidas na data do sistema
        // atualizar a tabela limpa a seleção; guarda qual linha estava marcada para marcar de novo
        ItemCatalogo marcado = tabelaCatalogo.getSelectionModel().getSelectedItem();
        List<ItemCatalogo> linhas = new ArrayList<>();
        for (Jogo jogo : jogoService.listar()) {
            if (jogo.getPlataformas() == null || jogo.getPlataformas().isEmpty()) {
                linhas.add(new ItemCatalogo(jogo, null)); // aparece, mas não dá para comprar/alugar
            } else {
                for (Plataforma plataforma : jogo.getPlataformas()) {
                    linhas.add(new ItemCatalogo(jogo, plataforma));
                }
            }
        }
        String pesquisa = campoPesquisaCatalogo.getText() == null ? "" : campoPesquisaCatalogo.getText().trim().toLowerCase();
        List<ItemCatalogo> filtradas = linhas.stream().filter(linha -> linha.jogo.getTitulo().toLowerCase().contains(pesquisa)
                || (linha.plataforma != null && linha.plataforma.getNome().toLowerCase().contains(pesquisa))).collect(Collectors.toList());
        tabelaCatalogo.getItems().setAll(filtradas);
        if (marcado != null) {
            for (ItemCatalogo linha : filtradas) {
                if (linha.jogo == marcado.jogo && linha.plataforma == marcado.plataforma) {
                    tabelaCatalogo.getSelectionModel().select(linha);
                    break;
                }
            }
        }
        auth.getUsuarioAtual().ifPresent(usuario -> {
            tabelaCompras.getItems().setAll(vendaService.listarDoUsuarioAtual());
            Contexto.get().getFinanceiroService().listarMultas(usuario); // atualiza multas de aluguéis atrasados
            tabelaLocacoes.getItems().setAll(usuario.getAlugueis() == null ? new ArrayList<>() : usuario.getAlugueis());
        });
        atualizarTotais();
    }

    private void atualizarTotais() {
        ItemCatalogo item = tabelaCatalogo.getSelectionModel().getSelectedItem();
        Cliente usuario = auth.getUsuarioAtual().orElse(null);
        if (item == null || usuario == null || item.plataforma == null) {
            rotuloTotalCompra.setText("Total da compra: -");
            rotuloTotalLocacao.setText("Total da locação: -");
            return;
        }
        if (!jogoService.estaCompleto(item.jogo)) {
            rotuloTotalCompra.setText("Total da compra: indisponível (cadastro do jogo incompleto)");
            rotuloTotalLocacao.setText("Total da locação: indisponível (cadastro do jogo incompleto)");
            return;
        }
        try {
            double total = financeiroService.calcularPrecoVenda(item.jogo, usuario) * spinnerQuantidade.getValue();
            rotuloTotalCompra.setText("Total da compra: " + dinheiro(total));
        } catch (IllegalArgumentException e) {
            rotuloTotalCompra.setText("Total da compra: indisponível (" + e.getMessage() + ")");
        }
        try {
            LocalDate hoje = Relogio.hoje();
            rotuloTotalLocacao.setText("Total da locação: " + dinheiro(
                    financeiroService.calcularValorLocacao(item.jogo, hoje, hoje.plusDays(spinnerDias.getValue()))));
        } catch (IllegalArgumentException e) {
            rotuloTotalLocacao.setText("Total da locação: indisponível (" + e.getMessage() + ")");
        }
    }

    private static String textoStatus(StatusAluguel status) {
        if (status == null) {
            return "-";
        }
        switch (status) {
            case ATIVO: return "Em dia";
            case ATRASADO: return "Atrasada";
            case DEVOLVIDO: return "Devolvida";
            default: return status.toString();
        }
    }

    /** Célula que mostra o texto do status como etiqueta colorida (cores em base.css: .etiqueta-*). */
    private static final class CelulaEtiqueta extends TableCell<Aluguel, String> {
        @Override
        protected void updateItem(String texto, boolean vazio) {
            super.updateItem(texto, vazio);
            setText(null);
            if (vazio || texto == null) {
                setGraphic(null);
                return;
            }
            Label etiqueta = new Label(texto);
            etiqueta.getStyleClass().add("etiqueta");
            etiqueta.getStyleClass().add(texto.equals("Atrasada") ? "etiqueta-alerta"
                    : texto.equals("Em dia") ? "etiqueta-ok" : "etiqueta-neutra");
            setGraphic(etiqueta);
        }
    }

    /** Uma linha do catálogo: um jogo em uma plataforma. */
    private static final class ItemCatalogo {
        final Jogo jogo;
        final Plataforma plataforma;

        ItemCatalogo(Jogo jogo, Plataforma plataforma) {
            this.jogo = jogo;
            this.plataforma = plataforma;
        }
    }

    // ---- formatação e alertas ----

    /** Preço de venda para o usuário atual; mostra o preço cheio ao lado quando há desconto. */
    private String textoPreco(Jogo jogo) {
        Cliente usuario = auth.getUsuarioAtual().orElse(null);
        if (usuario == null || jogo.getPreco() == null) {
            return dinheiro(jogo.getPreco());
        }
        try {
            double seu = financeiroService.calcularPrecoVenda(jogo, usuario);
            return seu < jogo.getPreco() ? dinheiro(seu) + " (de " + dinheiro(jogo.getPreco()) + ")" : dinheiro(seu);
        } catch (IllegalArgumentException e) {
            return "-";
        }
    }

    private static String dinheiro(Double valor) {
        return valor == null ? "-" : String.format(BR, "R$ %.2f", valor);
    }

    private static boolean confirmar(String titulo, String mensagem) {
        return Alertas.confirmar(titulo, mensagem);
    }

    private static void informar(String mensagem) {
        Alertas.informar(mensagem);
    }

    private static void avisar(String mensagem) {
        Alertas.avisar(mensagem);
    }
}

