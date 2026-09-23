package model;

public class Plataforma {

  private Long id;
  private String nome;
  private String fabricante;

  public Plataforma(Long id, String nome, String fabricante) {
    this.id = id;
    this.nome = nome;
    this.fabricante = fabricante;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getFabricante() {
    return fabricante;
  }

  public void setFabricante(String fabricante) {
    this.fabricante = fabricante;
  }
}
