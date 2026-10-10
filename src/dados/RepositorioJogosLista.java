package dados;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import model.Jogo;

/** Repositório em memória: guarda os jogos em uma lista. */
public class RepositorioJogosLista implements IRepositorioJogos {

  private final List<Jogo> jogos = new ArrayList<>();
  private long proximoId = 1;

  @Override
  public synchronized Jogo salvar(Jogo jogo) {
    if (jogo == null) {
      throw new IllegalArgumentException("O jogo é obrigatório.");
    }
    if (jogo.getId() == null) {
      jogo.setId(proximoId++);
    }
    jogos.add(jogo);
    return jogo;
  }

  @Override
  public synchronized void atualizar(Jogo jogo) {
    if (jogo == null || jogo.getId() == null) {
      throw new IllegalArgumentException("Só é possível atualizar um jogo já cadastrado.");
    }
    for (int i = 0; i < jogos.size(); i++) {
      if (jogos.get(i).getId().equals(jogo.getId())) {
        jogos.set(i, jogo);
        return;
      }
    }
    throw new IllegalArgumentException("Jogo não encontrado: id " + jogo.getId() + ".");
  }

  @Override
  public synchronized Optional<Jogo> buscarPorId(Long id) {
    for (Jogo jogo : jogos) {
      if (jogo.getId() != null && jogo.getId().equals(id)) {
        return Optional.of(jogo);
      }
    }
    return Optional.empty();
  }

  @Override
  public synchronized List<Jogo> listar() {
    return new ArrayList<>(jogos);
  }
}
