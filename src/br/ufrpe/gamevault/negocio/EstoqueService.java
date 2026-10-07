package br.ufrpe.gamevault.negocio;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import br.ufrpe.gamevault.dados.IRepositorioJogos;
import br.ufrpe.gamevault.negocio.beans.Jogo;
import br.ufrpe.gamevault.negocio.beans.Plataforma;

/**
 * REQ06 - controlar exemplares/licenças disponíveis por jogo e plataforma.
 * REQ07 - consultar disponibilidade de um jogo para locação em tempo real.
 *
 * O saldo fica em Jogo.estoquePorPlataforma (exemplares disponíveis agora). Toda operação
 * relê o jogo no repositório, então a consulta sempre reflete o estado atual.
 */
public class EstoqueService {

  private final IRepositorioJogos repositorioJogos;

  public EstoqueService(IRepositorioJogos repositorioJogos) {
    if (repositorioJogos == null) {
      throw new IllegalArgumentException("O repositório de jogos é obrigatório.");
    }
    this.repositorioJogos = repositorioJogos;
  }

  // ------------------------------------------------------------------ REQ06

  /** Define o saldo exato (cadastro inicial ou correção de inventário). */
  public synchronized void definirEstoque(Jogo jogo, Plataforma plataforma, int quantidade) {
    if (quantidade < 0) {
      throw new IllegalArgumentException("O estoque não pode ser negativo.");
    }
    Jogo atual = carregar(jogo);
    Plataforma chave = localizarChave(atual, plataforma);
    estoqueDe(atual).put(chave, quantidade);
    repositorioJogos.atualizar(atual);
  }

  /** Soma exemplares/licenças ao saldo (compra de novas unidades, devolução de locação). */
  public synchronized int adicionarExemplares(Jogo jogo, Plataforma plataforma, int quantidade) {
    exigirPositivo(quantidade);
    Jogo atual = carregar(jogo);
    Plataforma chave = localizarChave(atual, plataforma);
    Map<Plataforma, Integer> estoque = estoqueDe(atual);
    int novoSaldo = estoque.getOrDefault(chave, 0) + quantidade;
    estoque.put(chave, novoSaldo);
    repositorioJogos.atualizar(atual);
    return novoSaldo;
  }

  /** Tira exemplares do saldo (locação, venda, baixa); nunca deixa o saldo ficar negativo. */
  public synchronized int removerExemplares(Jogo jogo, Plataforma plataforma, int quantidade) {
    exigirPositivo(quantidade);
    Jogo atual = carregar(jogo);
    Plataforma chave = localizarChave(atual, plataforma);
    Map<Plataforma, Integer> estoque = estoqueDe(atual);
    int saldo = estoque.getOrDefault(chave, 0);
    if (saldo < quantidade) {
      throw new IllegalStateException("Estoque insuficiente de \"" + atual.getTitulo() + "\" para "
          + chave.getNome() + ": disponível " + saldo + ", solicitado " + quantidade + ".");
    }
    estoque.put(chave, saldo - quantidade);
    repositorioJogos.atualizar(atual);
    return saldo - quantidade;
  }

  /** Saldo atual de uma plataforma do jogo (0 se nunca foi cadastrado). */
  public synchronized int consultarQuantidade(Jogo jogo, Plataforma plataforma) {
    Jogo atual = carregar(jogo);
    Plataforma chave = localizarChave(atual, plataforma);
    return estoqueDe(atual).getOrDefault(chave, 0);
  }

  // ------------------------------------------------------------------ REQ07

  /** O jogo pode ser locado agora nesta plataforma? (saldo maior que zero) */
  public synchronized boolean estaDisponivel(Jogo jogo, Plataforma plataforma) {
    return consultarQuantidade(jogo, plataforma) > 0;
  }

  /** Saldo atual de todas as plataformas do jogo, na ordem em que o jogo as declara. */
  public synchronized Map<Plataforma, Integer> consultarDisponibilidade(Jogo jogo) {
    Jogo atual = carregar(jogo);
    Map<Plataforma, Integer> resultado = new LinkedHashMap<>();
    if (atual.getPlataformas() != null) {
      for (Plataforma plataforma : atual.getPlataformas()) {
        resultado.put(plataforma, consultarQuantidade(atual, plataforma));
      }
    }
    return resultado;
  }

  /** O jogo tem ao menos um exemplar disponível em alguma plataforma? */
  public synchronized boolean possuiAlgumaDisponibilidade(Jogo jogo) {
    for (int saldo : consultarDisponibilidade(jogo).values()) {
      if (saldo > 0) {
        return true;
      }
    }
    return false;
  }

  // ------------------------------------------------------------------ apoio

  private Jogo carregar(Jogo jogo) {
    if (jogo == null) {
      throw new IllegalArgumentException("O jogo é obrigatório.");
    }
    if (jogo.getId() == null) {
      throw new IllegalArgumentException("O jogo precisa estar cadastrado.");
    }
    return repositorioJogos.buscarPorId(jogo.getId()).orElseThrow(
        () -> new IllegalArgumentException("Jogo não encontrado: id " + jogo.getId() + "."));
  }

  private Map<Plataforma, Integer> estoqueDe(Jogo jogo) {
    if (jogo.getEstoquePorPlataforma() == null) {
      jogo.setEstoquePorPlataforma(new HashMap<>());
    }
    return jogo.getEstoquePorPlataforma();
  }

  /** Confirma que a plataforma pertence ao jogo e devolve a chave já usada no mapa (compara por id). */
  private Plataforma localizarChave(Jogo jogo, Plataforma plataforma) {
    if (plataforma == null) {
      throw new IllegalArgumentException("A plataforma é obrigatória.");
    }
    Plataforma canonica = null;
    if (jogo.getPlataformas() != null) {
      for (Plataforma candidata : jogo.getPlataformas()) {
        if (mesmaPlataforma(candidata, plataforma)) {
          canonica = candidata;
          break;
        }
      }
    }
    if (canonica == null) {
      throw new IllegalArgumentException("O jogo \"" + jogo.getTitulo()
          + "\" não está disponível para a plataforma " + plataforma.getNome() + ".");
    }
    for (Plataforma chave : estoqueDe(jogo).keySet()) {
      if (mesmaPlataforma(chave, canonica)) {
        return chave;
      }
    }
    return canonica;
  }

  private static boolean mesmaPlataforma(Plataforma a, Plataforma b) {
    if (a == b) {
      return true;
    }
    return a != null && b != null && a.getId() != null && a.getId().equals(b.getId());
  }

  private static void exigirPositivo(int quantidade) {
    if (quantidade <= 0) {
      throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
    }
  }
}
