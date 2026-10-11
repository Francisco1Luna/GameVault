package dados;

import java.util.List;
import java.util.Optional;

import model.Aluguel;
import model.Cliente;

public interface IRepositorioAlugueis {

  Aluguel salvar(Aluguel aluguel);

  void atualizar(Aluguel aluguel);

  Optional<Aluguel> buscarPorId(Long id);

  /** Todos os aluguéis do cliente (ativos e devolvidos). */
  List<Aluguel> listarPorCliente(Cliente cliente);

  List<Aluguel> listar();
}
