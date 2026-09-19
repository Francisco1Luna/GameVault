package model;

public class Plataforma {
  Long id;
  String nome;
  String fabricante;

  public Plataforma(Long id, String nome, String fabricante) {
    this.id = id;
    this.nome = nome;
    this.fabricante = fabricante;
  }

  public Long getId() {
    return id;
  }

  public String getNome() {
    return nome;
  }

  public String getFabricante() {
    return fabricante;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public void setFabricante(String fabricante) {
    this.fabricante = fabricante;
  }
}
