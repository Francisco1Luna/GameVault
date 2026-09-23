package model;

public class ItemVenda {

  private Jogo jogo;
  private Integer quantidade;
  private Double precoUnitario;

  public ItemVenda(Jogo jogo, Integer quantidade, Double precoUnitario) {
    this.jogo = jogo;
    this.quantidade = quantidade;
    this.precoUnitario = precoUnitario;
  }

  public Jogo getJogo() {
    return jogo;
  }

  public void setJogo(Jogo jogo) {
    this.jogo = jogo;
  }

  public Integer getQuantidade() {
    return quantidade;
  }

  public void setQuantidade(Integer quantidade) {
    this.quantidade = quantidade;
  }

  public Double getPrecoUnitario() {
    return precoUnitario;
  }

  public void setPrecoUnitario(Double precoUnitario) {
    this.precoUnitario = precoUnitario;
  }
}
