package model.enums;

import java.util.Objects;

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

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Plataforma)) {
      return false;
    }
    Plataforma other = (Plataforma) obj;
    if (id != null && other.id != null) {
      return id.equals(other.id);
    }
    return Objects.equals(nome, other.nome) && Objects.equals(fabricante, other.fabricante);
  }

  @Override
  public int hashCode() {
    if (id != null) {
      return Objects.hashCode(id);
    }
    return Objects.hash(nome, fabricante);
  }
}
