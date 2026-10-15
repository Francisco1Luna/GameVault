package negocio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dados.IRepositorioVendas;
import model.Cliente;
import model.ItemVenda;
import model.Venda;

/**
 * REQ10 - registrar venda de jogo (a venda é composta por itens: jogo, plataforma, quantidade, preço).
 * REQ11 - desconto automático para assinantes (calculado pelo FinanceiroService).
 * REQ22 - toda venda precisa ter ao menos um item antes de ser confirmada.
 */
public class VendaService {

  private final EstoqueService estoqueService;
  private final IRepositorioVendas repositorioVendas;
  private final FinanceiroService financeiroService;

  public VendaService(EstoqueService estoqueService, IRepositorioVendas repositorioVendas) {
    this(estoqueService, repositorioVendas, new FinanceiroService());
  }

  public VendaService(EstoqueService estoqueService, IRepositorioVendas repositorioVendas,
      FinanceiroService financeiroService) {
    if (estoqueService == null) {
      throw new IllegalArgumentException("O serviço de estoque é obrigatório.");
    }
    if (repositorioVendas == null) {
      throw new IllegalArgumentException("O repositório de vendas é obrigatório.");
    }
    if (financeiroService == null) {
      throw new IllegalArgumentException("O serviço financeiro é obrigatório.");
    }
    this.estoqueService = estoqueService;
    this.repositorioVendas = repositorioVendas;
    this.financeiroService = financeiroService;
  }

  /**
   * Confirma a venda. De cada item do pedido só são lidos jogo, plataforma e quantidade: o preço
   * unitário informado é ignorado, porque quem calcula (com o desconto do assinante) é o serviço.
   * O preço gravado em cada item da venda é o que foi cobrado e não muda se o jogo mudar de preço.
   * Se algum item não tiver estoque, ou se o salvamento falhar, o estoque já baixado volta.
   */
  public synchronized Venda registrarVenda(Cliente cliente, List<ItemVenda> pedido, LocalDate dataVenda) {
    if (cliente == null) {
      throw new IllegalArgumentException("O cliente é obrigatório.");
    }
    if (dataVenda == null) {
      throw new IllegalArgumentException("A data da venda é obrigatória.");
    }
    if (pedido == null || pedido.isEmpty()) {
      throw new IllegalArgumentException("A venda precisa ter ao menos um item.");
    }

    // 1) monta e valida todos os itens antes de mexer no estoque
    List<ItemVenda> itens = new ArrayList<>();
    double total = 0.0;
    for (ItemVenda solicitado : pedido) {
      if (solicitado == null) {
        throw new IllegalArgumentException("A venda contém um item nulo.");
      }
      if (solicitado.getJogo() == null) {
        throw new IllegalArgumentException("Todo item da venda precisa de um jogo.");
      }
      if (solicitado.getPlataforma() == null) {
        throw new IllegalArgumentException("Todo item da venda precisa de uma plataforma.");
      }
      if (solicitado.getQuantidade() == null || solicitado.getQuantidade() <= 0) {
        throw new IllegalArgumentException("A quantidade de cada item deve ser maior que zero.");
      }
      double precoUnitario = financeiroService.calcularPrecoVenda(solicitado.getJogo(), cliente);
      itens.add(new ItemVenda(solicitado.getJogo(), solicitado.getPlataforma(), solicitado.getQuantidade(),
          precoUnitario));
      total += precoUnitario * solicitado.getQuantidade();
    }
    double valorTotal = Math.round(total * 100.0) / 100.0;

    // 2) baixa o estoque item a item; se um falhar, devolve os que já saíram
    List<ItemVenda> baixados = new ArrayList<>();
    try {
      for (ItemVenda item : itens) {
        estoqueService.removerExemplares(item.getJogo(), item.getPlataforma(), item.getQuantidade());
        baixados.add(item);
      }
      return repositorioVendas.salvar(new Venda(null, cliente, dataVenda, itens, valorTotal));
    } catch (RuntimeException e) {
      devolverEstoque(baixados);
      throw e;
    }
  }

  public Optional<Venda> buscarPorId(Long id) {
    if (id == null) {
      return Optional.empty();
    }
    return repositorioVendas.buscarPorId(id);
  }

  public List<Venda> listarPorCliente(Cliente cliente) {
    if (cliente == null) {
      throw new IllegalArgumentException("O cliente é obrigatório.");
    }
    return repositorioVendas.listarPorCliente(cliente);
  }

  public List<Venda> listar() {
    return repositorioVendas.listar();
  }

  private void devolverEstoque(List<ItemVenda> baixados) {
    for (ItemVenda item : baixados) {
      estoqueService.adicionarExemplares(item.getJogo(), item.getPlataforma(), item.getQuantidade());
    }
  }
}
