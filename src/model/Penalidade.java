package model;

import java.time.LocalDate;

public class Penalidade {

  private Long id;
  private Aluguel aluguelOrigem;
  private Double valor;
  private LocalDate dataGeracao;
  private boolean paga;
  private LocalDate dataPagamento;

  public Penalidade(Long id, Aluguel aluguelOrigem, Double valor, LocalDate dataGeracao, boolean paga,
      LocalDate dataPagamento) {
    this.id = id;
    this.aluguelOrigem = aluguelOrigem;
    this.valor = valor;
    this.dataGeracao = dataGeracao;
    this.paga = paga;
    this.dataPagamento = dataPagamento;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Aluguel getAluguelOrigem() {
    return aluguelOrigem;
  }

  public void setAluguelOrigem(Aluguel aluguelOrigem) {
    this.aluguelOrigem = aluguelOrigem;
  }

  public Double getValor() {
    return valor;
  }

  public void setValor(Double valor) {
    this.valor = valor;
  }

  public LocalDate getDataGeracao() {
    return dataGeracao;
  }

  public void setDataGeracao(LocalDate dataGeracao) {
    this.dataGeracao = dataGeracao;
  }

  public boolean isPaga() {
    return paga;
  }

  public void setPaga(boolean paga) {
    this.paga = paga;
  }

  public LocalDate getDataPagamento() {
    return dataPagamento;
  }

  public void setDataPagamento(LocalDate dataPagamento) {
    this.dataPagamento = dataPagamento;
  }
}
