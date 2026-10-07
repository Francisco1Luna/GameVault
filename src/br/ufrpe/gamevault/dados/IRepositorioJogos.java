package br.ufrpe.gamevault.dados;

import java.util.List;
import java.util.Optional;

import br.ufrpe.gamevault.negocio.beans.Jogo;

public interface IRepositorioJogos {

  Jogo salvar(Jogo jogo);

  void atualizar(Jogo jogo);

  Optional<Jogo> buscarPorId(Long id);

  List<Jogo> listar();
}
