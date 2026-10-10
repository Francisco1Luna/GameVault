package gui;

import dados.RepositorioJogosLista;
import negocio.EstoqueService;
import model.Jogo;
import model.enums.Plataforma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Principal {

  public static void main(String[] args) {
    RepositorioJogosLista repo = new RepositorioJogosLista();
    Plataforma p1 = new Plataforma(1L, "PC", "MS");
    Plataforma p2 = new Plataforma(1L, "PC", "MS");
    Jogo jogo = new Jogo(1L, "T", null, null, null, null, null, new ArrayList<>(Arrays.asList(p1)), new HashMap<>(), null, null);
    repo.salvar(jogo);
    EstoqueService svc = new EstoqueService(repo);
    try {
      System.out.println("adicionar=" + svc.adicionarExemplares(jogo, p2, 2));
      System.out.println("consultar=" + svc.consultarQuantidade(jogo, p1));
    } catch (Exception e) { e.printStackTrace(); }
  }
}
