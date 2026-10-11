package dados;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import model.Cliente;

/** Repositório em memória: guarda os clientes (padrão e assinantes) em uma lista. */
public class RepositorioClientesLista implements IRepositorioClientes {

  private final List<Cliente> clientes = new ArrayList<>();
  private long proximoId = 1;

  @Override
  public synchronized Cliente salvar(Cliente cliente) {
    if (cliente == null) {
      throw new IllegalArgumentException("O cliente é obrigatório.");
    }
    if (cliente.getId() == null) {
      cliente.setId(proximoId++);
    } else {
      if (buscarPorId(cliente.getId()).isPresent()) {
        throw new IllegalArgumentException("Já existe cliente com o id " + cliente.getId() + ".");
      }
      proximoId = Math.max(proximoId, cliente.getId() + 1); // evita gerar depois um id já usado
    }
    clientes.add(cliente);
    return cliente;
  }

  @Override
  public synchronized void atualizar(Cliente cliente) {
    if (cliente == null || cliente.getId() == null) {
      throw new IllegalArgumentException("Só é possível atualizar um cliente já cadastrado.");
    }
    for (int i = 0; i < clientes.size(); i++) {
      if (clientes.get(i).getId().equals(cliente.getId())) {
        clientes.set(i, cliente);
        return;
      }
    }
    throw new IllegalArgumentException("Cliente não encontrado: id " + cliente.getId() + ".");
  }

  @Override
  public synchronized boolean remover(Long id) {
    for (int i = 0; i < clientes.size(); i++) {
      if (clientes.get(i).getId() != null && clientes.get(i).getId().equals(id)) {
        clientes.remove(i);
        return true;
      }
    }
    return false;
  }

  @Override
  public synchronized Optional<Cliente> buscarPorId(Long id) {
    for (Cliente cliente : clientes) {
      if (cliente.getId() != null && cliente.getId().equals(id)) {
        return Optional.of(cliente);
      }
    }
    return Optional.empty();
  }

  @Override
  public synchronized Optional<Cliente> buscarPorCpf(String cpf) {
    if (cpf == null) {
      return Optional.empty();
    }
    for (Cliente cliente : clientes) {
      if (cpf.equals(cliente.getCpf())) {
        return Optional.of(cliente);
      }
    }
    return Optional.empty();
  }

  @Override
  public synchronized List<Cliente> listar() {
    return new ArrayList<>(clientes);
  }
}
