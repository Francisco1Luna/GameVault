package dados;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import model.Cliente;
import model.Venda;

/** Repositório em memória: guarda as vendas em uma lista e gera os ids. */
public class RepositorioVendasLista implements IRepositorioVendas {

  private final List<Venda> vendas = new ArrayList<>();
  private long proximoId = 1;

  @Override
  public synchronized Venda salvar(Venda venda) {
    if (venda == null) {
      throw new IllegalArgumentException("A venda é obrigatória.");
    }
    if (venda.getId() == null) {
      venda.setId(proximoId++);
    } else {
      if (buscarPorId(venda.getId()).isPresent()) {
        throw new IllegalArgumentException("Já existe venda com o id " + venda.getId() + ".");
      }
      proximoId = Math.max(proximoId, venda.getId() + 1); // evita gerar depois um id já usado
    }
    vendas.add(venda);
    return venda;
  }

  @Override
  public synchronized Optional<Venda> buscarPorId(Long id) {
    for (Venda venda : vendas) {
      if (venda.getId() != null && venda.getId().equals(id)) {
        return Optional.of(venda);
      }
    }
    return Optional.empty();
  }

  @Override
  public synchronized List<Venda> listarPorCliente(Cliente cliente) {
    List<Venda> resultado = new ArrayList<>();
    for (Venda venda : vendas) {
      if (mesmoCliente(venda.getCliente(), cliente)) {
        resultado.add(venda);
      }
    }
    return resultado;
  }

  @Override
  public synchronized List<Venda> listar() {
    return new ArrayList<>(vendas);
  }

  private static boolean mesmoCliente(Cliente a, Cliente b) {
    if (a == b) {
      return a != null;
    }
    return a != null && b != null && a.getId() != null && a.getId().equals(b.getId());
  }
}
