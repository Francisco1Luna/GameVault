package model;
import java.time.LocalDate;
import ENUMS.StatusAluguel;

public class Aluguel {
  private Long id;
  private Jogo jogo;
  private Plataforma plataforma;
  private LocalDate dataInicio;
  private StatusAluguel statusAluguel;
  private LocalDate dataDevolucaoEfetiva;
  private LocalDate dataDevolucaoPrevista;
  private Cliente cliente;

  public Aluguel(Long id, Jogo jogo, Plataforma plataforma, LocalDate dataInicio, StatusAluguel statusAluguel,
      LocalDate dataDevolucaoEfetiva, LocalDate dataDevolucaoPrevista, Cliente cliente) {
    this.id = id;
    this.jogo = jogo;
    this.plataforma = plataforma;
    this.dataInicio = dataInicio;
    this.statusAluguel = statusAluguel;
    this.dataDevolucaoEfetiva = dataDevolucaoEfetiva;
    this.dataDevolucaoPrevista = dataDevolucaoPrevista;
    this.cliente = cliente;
  }

  public Long getId() {
    return id;
  }

  public Jogo getJogo() {
    return jogo;
  }

  public Plataforma getPlataforma() {
    return plataforma;
  }

  public LocalDate getDataInicio() {
    return dataInicio;
  }

  public StatusAluguel getStatusAluguel() {
    return statusAluguel;
  }

  public LocalDate getDataDevolucaoEfetiva() {
    return dataDevolucaoEfetiva;
  }

  public LocalDate getDataDevolucaoPrevista() {
    return dataDevolucaoPrevista;
  }

  public Cliente getCliente() {
    return cliente;
  }

  public int getDiasAtraso() {
    if (dataDevolucaoEfetiva != null && dataDevolucaoPrevista != null) {
      return (int) java.time.temporal.ChronoUnit.DAYS.between(dataDevolucaoPrevista, dataDevolucaoEfetiva);
    }
    return 0;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setJogo(Jogo jogo) {
    this.jogo = jogo;
  }

  public void setPlataforma(Plataforma plataforma) {
    this.plataforma = plataforma;
  }

  public void setDataInicio(LocalDate dataInicio) {
    this.dataInicio = dataInicio;
  }

  public void setStatusAluguel(StatusAluguel statusAluguel) {
    this.statusAluguel = statusAluguel;
  }

  public void setDataDevolucaoEfetiva(LocalDate dataDevolucaoEfetiva) {
    this.dataDevolucaoEfetiva = dataDevolucaoEfetiva;
  }

  public void setDataDevolucaoPrevista(LocalDate dataDevolucaoPrevista) {
    this.dataDevolucaoPrevista = dataDevolucaoPrevista;
  }

  public void setCliente(Cliente cliente) {
    this.cliente = cliente;
  }




}
