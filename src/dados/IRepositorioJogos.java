package dados;

import java.util.List;
import java.util.Optional;

import model.Jogo;

public interface IRepositorioJogos {

  Jogo salvar(Jogo jogo);

  void atualizar(Jogo jogo);

  Optional<Jogo> buscarPorId(Long id);

  List<Jogo> listar();
}
