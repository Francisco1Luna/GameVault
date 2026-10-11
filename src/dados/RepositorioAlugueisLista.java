package dados;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import model.Aluguel;
import model.Cliente;

/**
 * Repositório em memória: guarda os aluguéis em uma lista. É ele quem gera os ids, então todos os
 * serviços devem compartilhar a mesma instância.
 */
public class RepositorioAlugueisLista implements IRepositorioAlugueis {

  private final List<Aluguel> alugueis = new ArrayList<>();
  private long proximoId = 1;

  @Override
  public synchronized Aluguel salvar(Aluguel aluguel) {
    if (aluguel == null) {
      throw new IllegalArgumentException("O aluguel é obrigatório.");
    }
    if (aluguel.getId() == null) {
      aluguel.setId(proximoId++);
    } else {
      if (buscarPorId(aluguel.getId()).isPresent()) {
        throw new IllegalArgumentException("Já existe aluguel com o id " + aluguel.getId() + ".");
      }
      proximoId = Math.max(proximoId, aluguel.getId() + 1); // evita gerar depois um id já usado
    }
    alugueis.add(aluguel);
    return aluguel;
  }

  @Override
  public synchronized void atualizar(Aluguel aluguel) {
    if (aluguel == null || aluguel.getId() == null) {
      throw new IllegalArgumentException("Só é possível atualizar um aluguel já cadastrado.");
    }
    for (int i = 0; i < alugueis.size(); i++) {
      if (alugueis.get(i).getId().equals(aluguel.getId())) {
        alugueis.set(i, aluguel);
        return;
      }
    }
    throw new IllegalArgumentException("Aluguel não encontrado: id " + aluguel.getId() + ".");
  }

  @Override
  public synchronized Optional<Aluguel> buscarPorId(Long id) {
    for (Aluguel aluguel : alugueis) {
      if (aluguel.getId() != null && aluguel.getId().equals(id)) {
        return Optional.of(aluguel);
      }
    }
    return Optional.empty();
  }

  @Override
  public synchronized List<Aluguel> listarPorCliente(Cliente cliente) {
    List<Aluguel> resultado = new ArrayList<>();
    for (Aluguel aluguel : alugueis) {
      if (mesmoCliente(aluguel.getCliente(), cliente)) {
        resultado.add(aluguel);
      }
    }
    return resultado;
  }

  @Override
  public synchronized List<Aluguel> listar() {
    return new ArrayList<>(alugueis);
  }

  private static boolean mesmoCliente(Cliente a, Cliente b) {
    if (a == b) {
      return a != null;
    }
    return a != null && b != null && a.getId() != null && a.getId().equals(b.getId());
  }
}
