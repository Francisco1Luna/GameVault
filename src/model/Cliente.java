package model;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public abstract class Cliente {

  private Long id;
  private String nome;
  private String cpf;
  private LocalDate dataNascimento;
  private String email;
  private String telefone;
  private List<Aluguel> alugueisAtivos = new ArrayList<>();
  private List<Penalidade> penalidades = new ArrayList<>();

  public Cliente(Long id, String nome, String cpf, LocalDate dataNascimento, String email, String telefone) {
    this.id = id;
    this.nome = nome;
    this.cpf = cpf;
    this.dataNascimento = dataNascimento;
    this.email = email;
    this.telefone = telefone;
  }

  // Cada subclasse (ClientePadrao, ClienteAssinante) define seu próprio limite
  // de aluguéis simultâneos e sua própria regra de desconto em vendas.
  public abstract int getLimiteAluguelSimultaneo();

  public abstract double aplicarDesconto(double valorBruto);

  public int getIdade() {
    return Period.between(dataNascimento, LocalDate.now()).getYears();
  }

  
  public double getDebitoAcumulado() {
    double total = 0.0;
    for (Penalidade penalidade : penalidades) {
      if (!penalidade.isPaga()) {
        total += penalidade.getValor();
      }
    }
    return total;
  }

  public boolean temPenalidadePendente() {
    return getDebitoAcumulado() > 0;
  }

  public boolean podeAlugar() {
    return alugueisAtivos.size() < getLimiteAluguelSimultaneo();
  }

  public void adicionarAluguelAtivo(Aluguel aluguel) {
    alugueisAtivos.add(aluguel);
  }

  public void removerAluguelAtivo(Aluguel aluguel) {
    alugueisAtivos.remove(aluguel);
  }

  public void adicionarPenalidade(Penalidade penalidade) {
    penalidades.add(penalidade);
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

  /**
   * Retorna a lista de aluguéis ativos do cliente.
   * @return Lista de aluguéis ativos.
   */
  
  public List<Aluguel> getAlugueisAtivos() {
    return alugueisAtivos;
  }

  public List<Penalidade> getPenalidades() {
    return penalidades;
  }
}
