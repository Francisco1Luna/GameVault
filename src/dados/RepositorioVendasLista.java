package dados;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import model.Venda;

/** Repositório em memória: guarda as vendas em uma lista. */
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
      proximoId = Math.max(proximoId, venda.getId() + 1);
    }
    vendas.add(venda);
    return venda;
  }

  @Override
  public synchronized void atualizar(Venda venda) {
    if (venda == null || venda.getId() == null) {
      throw new IllegalArgumentException("Só é possível atualizar uma venda já cadastrada.");
    }
    for (int i = 0; i < vendas.size(); i++) {
      if (vendas.get(i).getId().equals(venda.getId())) {
        vendas.set(i, venda);
        return;
      }
    }
    throw new IllegalArgumentException("Venda não encontrada: id " + venda.getId() + ".");
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
  public synchronized List<Venda> listar() {
    return new ArrayList<>(vendas);
  }
}
