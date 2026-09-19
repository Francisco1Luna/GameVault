package model;

import java.time.LocalDate;

public class Penalidade {

  private Long id;
  private Aluguel aluguelOrigem;
  private double valor;
  private LocalDate dataGeracao;
  private boolean paga;
  private LocalDate dataPagamento;

  public Penalidade(Long id, Aluguel aluguelOrigem, double valor, LocalDate dataGeracao) {
    this.id = id;
    this.aluguelOrigem = aluguelOrigem;
    this.valor = valor;
    this.dataGeracao = dataGeracao;
    this.paga = false;
  }

 
  public static double calcularValor(int diasAtraso, double tarifaDiaria) {
    if (diasAtraso <= 0) {
      return 0.0;
    }
    return diasAtraso * tarifaDiaria;
  }

  
  public void quitar() {
    this.paga = true;
    this.dataPagamento = LocalDate.now();
  }

  public Long getId() {
    return id;
  }

  public Aluguel getAluguelOrigem() {
    return aluguelOrigem;
  }

  public double getValor() {
    return valor;
  }

  public LocalDate getDataGeracao() {
    return dataGeracao;
  }

  public boolean isPaga() {
    return paga;
  }

  public LocalDate getDataPagamento() {
    return dataPagamento;
  }
}
