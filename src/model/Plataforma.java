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

  /**
   * Duas plataformas são iguais quando são o mesmo objeto ou quando têm o mesmo id. Plataforma sem
   * id só é igual a si mesma, e o hashCode segue a mesma regra, então equals e hashCode combinam.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Plataforma)) {
      return false;
    }
    Plataforma other = (Plataforma) obj;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : System.identityHashCode(this);
  }
}
