package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Cliente {

  private Long id;
  private String nome;
  private String cpf;
  private LocalDate dataNascimento;
  private String email;
  private String telefone;
  private int limiteAluguelSimultaneo;
  private List<Aluguel> alugueisAtivos = new ArrayList<>();
  private List<Penalidade> penalidades = new ArrayList<>();

  public Cliente(Long id, String nome, String cpf, LocalDate dataNascimento, String email, String telefone,
      int limiteAluguelSimultaneo) {
    this.id = id;
    this.nome = nome;
    this.cpf = cpf;
    this.dataNascimento = dataNascimento;
    this.email = email;
    this.telefone = telefone;
    this.limiteAluguelSimultaneo = limiteAluguelSimultaneo;
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

  public String getCpf() {
    return cpf;
  }

  public void setCpf(String cpf) {
    this.cpf = cpf;
  }

  public LocalDate getDataNascimento() {
    return dataNascimento;
  }

  public void setDataNascimento(LocalDate dataNascimento) {
    this.dataNascimento = dataNascimento;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getTelefone() {
    return telefone;
  }

  public void setTelefone(String telefone) {
    this.telefone = telefone;
  }

  public int getLimiteAluguelSimultaneo() {
    return limiteAluguelSimultaneo;
  }

  public void setLimiteAluguelSimultaneo(int limiteAluguelSimultaneo) {
    this.limiteAluguelSimultaneo = limiteAluguelSimultaneo;
  }

  public List<Aluguel> getAlugueisAtivos() {
    return alugueisAtivos;
  }

  public void setAlugueisAtivos(List<Aluguel> alugueisAtivos) {
    this.alugueisAtivos = alugueisAtivos;
  }

  public List<Penalidade> getPenalidades() {
    return penalidades;
  }

  public void setPenalidades(List<Penalidade> penalidades) {
    this.penalidades = penalidades;
  }
}
