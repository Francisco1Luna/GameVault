package dados;

import java.util.List;
import java.util.Optional;

import model.Cliente;
import model.Penalidade;

public interface IRepositorioPenalidades {

  Penalidade salvar(Penalidade penalidade);

  void atualizar(Penalidade penalidade);

  Optional<Penalidade> buscarPorId(Long id);

  /** Penalidades geradas por aluguéis do cliente (pagas ou não). */
  List<Penalidade> listarPorCliente(Cliente cliente);

  /** Penalidade já gerada para o aluguel informado, se existir. */
  Optional<Penalidade> buscarPorAluguel(Long idAluguel);
}
