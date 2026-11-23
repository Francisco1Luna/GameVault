package negocio;

import dados.RepositorioJogosLista;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import dados.IRepositorioJogos;
import model.Jogo;
import model.Plataforma;

/**
 * REQ06 - controlar exemplares/licenças disponíveis por jogo e plataforma.
 * REQ07 - consultar disponibilidade de um jogo para locação em tempo real.
 *
 * O saldo fica em Jogo.estoquePorPlataforma (exemplares disponíveis agora). Toda operação
 * relê o jogo no repositório, então a consulta sempre reflete o estado atual.
 */
public class EstoqueService {

  // Singleton: uma única instância em memória; use EstoqueService.getInstance().
  private static final EstoqueService INSTANCIA = new EstoqueService(
    RepositorioJogosLista.getInstance());

  public static EstoqueService getInstance() {
    return INSTANCIA;
  }

  /** Limite do ajuste manual por plataforma (evita digitar 999999999 por engano). */
  public static final int ESTOQUE_MAXIMO = 99_999;

  private final IRepositorioJogos repositorioJogos;

  /**
   * Padrão Observer: quem quer saber quando o estoque muda (ex.: a tela de Jogos) se registra aqui
   * e é avisado depois de cada venda, locação, devolução, ajuste manual ou alteração de jogo.
   * CopyOnWriteArrayList: dá para adicionar/remover ouvinte enquanto a lista é percorrida.
   */
  private final List<Runnable> ouvintes = new CopyOnWriteArrayList<>();

  private EstoqueService(IRepositorioJogos repositorioJogos) {
    if (repositorioJogos == null) {
      throw new IllegalArgumentException("O repositório de jogos é obrigatório.");
    }
    this.repositorioJogos = repositorioJogos;
  }

  // ------------------------------------------------------------------ observadores

  public void adicionarOuvinte(Runnable ouvinte) {
    if (ouvinte != null) {
      ouvintes.add(ouvinte);
    }
  }

  public void removerOuvinte(Runnable ouvinte) {
    ouvintes.remove(ouvinte);
  }

  /** Avisa os ouvintes que o estoque mudou. Também é chamado pelo JogoService (cadastro/remoção de jogo). */
  public void notificarMudanca() {
    for (Runnable ouvinte : ouvintes) {
      ouvinte.run();
    }
  }

  /** Soma dos exemplares disponíveis de um jogo em todas as plataformas (0 se não tiver estoque). */
  public synchronized long quantidadeTotal(Jogo jogo) {
    Jogo atual = carregar(jogo);
    long total = 0; // long: a soma de várias plataformas não estoura o limite do int
    for (Integer quantidade : estoqueDe(atual).values()) {
      total += quantidade == null ? 0 : quantidade;
    }
    return total;
  }

  // ------------------------------------------------------------------ REQ06

  /** Define o saldo exato (cadastro inicial ou correção de inventário). Só administrador. */
  public synchronized void definirEstoque(Jogo jogo, Plataforma plataforma, int quantidade) {
    Sessao.exigirAdministrador();
    if (quantidade < 0) {
      throw new IllegalArgumentException("O estoque não pode ser negativo.");
    }
    if (quantidade > ESTOQUE_MAXIMO) {
      throw new IllegalArgumentException("O estoque máximo por plataforma é " + ESTOQUE_MAXIMO + " unidades.");
    }
    Jogo atual = carregar(jogo);
    Plataforma chave = localizarChave(atual, plataforma);
    estoqueDe(atual).put(chave, quantidade);
    repositorioJogos.atualizar(atual);
    notificarMudanca();
  }

  /**
   * Soma exemplares/licenças ao saldo (compra de novas unidades, devolução de locação). Aceita uma
   * plataforma que o jogo já não lista (foi desmarcada na edição enquanto havia locação em
   * andamento): senão a devolução desse exemplar ficaria travada para sempre.
   */
  public synchronized int adicionarExemplares(Jogo jogo, Plataforma plataforma, int quantidade) {
    exigirPositivo(quantidade);
    Jogo atual = carregar(jogo);
    Plataforma chave = localizarChave(atual, plataforma, false);
    Map<Plataforma, Integer> estoque = estoqueDe(atual);
    // addExact: se a soma passar do limite do int, dá erro em vez de virar número negativo
    int novoSaldo;
    try {
      novoSaldo = Math.addExact(estoque.getOrDefault(chave, 0), quantidade);
    } catch (ArithmeticException e) {
      throw new IllegalStateException("O estoque de \"" + atual.getTitulo() + "\" passaria do limite permitido.");
    }
    estoque.put(chave, novoSaldo);
    repositorioJogos.atualizar(atual);
    notificarMudanca();
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
    notificarMudanca();
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
    return localizarChave(jogo, plataforma, true);
  }

  /** Com {@code exigirNoJogo} false, uma plataforma fora da lista do jogo é aceita (usa a própria). */
  private Plataforma localizarChave(Jogo jogo, Plataforma plataforma, boolean exigirNoJogo) {
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
      if (exigirNoJogo) {
        throw new IllegalArgumentException("O jogo \"" + jogo.getTitulo()
            + "\" não está disponível para a plataforma " + plataforma.getNome() + ".");
      }
      canonica = plataforma;
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
