package dados;

import java.util.List;
import java.util.Optional;

import model.Cliente;

public interface IRepositorioClientes {

  Cliente salvar(Cliente cliente);

  void atualizar(Cliente cliente);

  /** Remove o cliente e devolve true se ele existia. */
  boolean remover(Long id);

  Optional<Cliente> buscarPorId(Long id);

  Optional<Cliente> buscarPorCpf(String cpf);

  List<Cliente> listar();
}
