package br.ufrpe.gamevault.dados;

import java.util.List;
import java.util.Optional;

import br.ufrpe.gamevault.negocio.beans.Cliente;
import br.ufrpe.gamevault.negocio.beans.Penalidade;

public interface IRepositorioPenalidades {

  Penalidade salvar(Penalidade penalidade);

  void atualizar(Penalidade penalidade);

  Optional<Penalidade> buscarPorId(Long id);

  /** Penalidades geradas por aluguéis do cliente (pagas ou não). */
  List<Penalidade> listarPorCliente(Cliente cliente);

  /** Penalidade já gerada para o aluguel informado, se existir. */
  Optional<Penalidade> buscarPorAluguel(Long idAluguel);
}
