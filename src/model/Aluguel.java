package model;

import java.time.LocalDate;

import ENUMS.StatusAluguel;

public class Aluguel {

  private Long id;
  private Cliente cliente;
  private Jogo jogo;
  private Plataforma plataforma;
  private LocalDate dataInicio;
  private LocalDate dataDevolucaoPrevista;
  private LocalDate dataDevolucaoEfetiva;
  private StatusAluguel status;

  public Aluguel(Long id, Cliente cliente, Jogo jogo, Plataforma plataforma, LocalDate dataInicio,
      LocalDate dataDevolucaoPrevista, LocalDate dataDevolucaoEfetiva, StatusAluguel status) {
    this.id = id;
    this.cliente = cliente;
    this.jogo = jogo;
    this.plataforma = plataforma;
    this.dataInicio = dataInicio;
    this.dataDevolucaoPrevista = dataDevolucaoPrevista;
    this.dataDevolucaoEfetiva = dataDevolucaoEfetiva;
    this.status = status;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Cliente getCliente() {
    return cliente;
  }

  public void setCliente(Cliente cliente) {
    this.cliente = cliente;
  }

  public Jogo getJogo() {
    return jogo;
  }

  public void setJogo(Jogo jogo) {
    this.jogo = jogo;
  }

  public Plataforma getPlataforma() {
    return plataforma;
  }

  public void setPlataforma(Plataforma plataforma) {
    this.plataforma = plataforma;
  }

  public LocalDate getDataInicio() {
    return dataInicio;
  }

  public void setDataInicio(LocalDate dataInicio) {
    this.dataInicio = dataInicio;
  }

  public LocalDate getDataDevolucaoPrevista() {
    return dataDevolucaoPrevista;
  }

  public void setDataDevolucaoPrevista(LocalDate dataDevolucaoPrevista) {
    this.dataDevolucaoPrevista = dataDevolucaoPrevista;
  }

  public LocalDate getDataDevolucaoEfetiva() {
    return dataDevolucaoEfetiva;
  }

  public void setDataDevolucaoEfetiva(LocalDate dataDevolucaoEfetiva) {
    this.dataDevolucaoEfetiva = dataDevolucaoEfetiva;
  }

  public StatusAluguel getStatus() {
    return status;
  }

  public void setStatus(StatusAluguel status) {
    this.status = status;
  }
}
