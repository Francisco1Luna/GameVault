package br.ufrpe.gamevault.dados;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.ufrpe.gamevault.negocio.beans.Cliente;
import br.ufrpe.gamevault.negocio.beans.Penalidade;

/** Repositório em memória: guarda as penalidades em uma lista. */
public class RepositorioPenalidadesLista implements IRepositorioPenalidades {

  private final List<Penalidade> penalidades = new ArrayList<>();
  private long proximoId = 1;

  @Override
  public synchronized Penalidade salvar(Penalidade penalidade) {
    if (penalidade == null) {
      throw new IllegalArgumentException("A penalidade é obrigatória.");
    }
    if (penalidade.getId() == null) {
      penalidade.setId(proximoId++);
    }
    penalidades.add(penalidade);
    return penalidade;
  }

  @Override
  public synchronized void atualizar(Penalidade penalidade) {
    if (penalidade == null || penalidade.getId() == null) {
      throw new IllegalArgumentException("Só é possível atualizar uma penalidade já cadastrada.");
    }
    for (int i = 0; i < penalidades.size(); i++) {
      if (penalidades.get(i).getId().equals(penalidade.getId())) {
        penalidades.set(i, penalidade);
        return;
      }
    }
    throw new IllegalArgumentException("Penalidade não encontrada: id " + penalidade.getId() + ".");
  }

  @Override
  public synchronized Optional<Penalidade> buscarPorId(Long id) {
    for (Penalidade p : penalidades) {
      if (p.getId() != null && p.getId().equals(id)) {
        return Optional.of(p);
      }
    }
    return Optional.empty();
  }

  @Override
  public synchronized List<Penalidade> listarPorCliente(Cliente cliente) {
    List<Penalidade> resultado = new ArrayList<>();
    for (Penalidade p : penalidades) {
      if (p.getAluguelOrigem() != null && mesmoCliente(p.getAluguelOrigem().getCliente(), cliente)) {
        resultado.add(p);
      }
    }
    return resultado;
  }

  @Override
  public synchronized Optional<Penalidade> buscarPorAluguel(Long idAluguel) {
    for (Penalidade p : penalidades) {
      if (p.getAluguelOrigem() != null && p.getAluguelOrigem().getId() != null
          && p.getAluguelOrigem().getId().equals(idAluguel)) {
        return Optional.of(p);
      }
    }
    return Optional.empty();
  }

  private static boolean mesmoCliente(Cliente a, Cliente b) {
    if (a == b) {
      return true;
    }
    return a != null && b != null && a.getId() != null && a.getId().equals(b.getId());
  }
}
