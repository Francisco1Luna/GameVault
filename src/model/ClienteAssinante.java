package model;

import java.time.LocalDate;

public class ClienteAssinante extends Cliente {

  private static final int LIMITE_ALUGUEL_SIMULTANEO = 3;

  private double percentualDesconto;
  private LocalDate dataAdesao;

  public ClienteAssinante(Long id, String nome, String cpf, LocalDate dataNascimento, String email, String telefone,
      double percentualDesconto, LocalDate dataAdesao) {
    super(id, nome, cpf, dataNascimento, email, telefone);
    this.percentualDesconto = percentualDesconto;
    this.dataAdesao = dataAdesao;
  }
  
  public int getLimiteAluguelSimultaneo() {
    return LIMITE_ALUGUEL_SIMULTANEO;
  }

  @Override
  public double aplicarDesconto(double valorBruto) {
    return valorBruto * (1 - percentualDesconto);
  }

  public double getPercentualDesconto() {
    return percentualDesconto;
  }

  public void setPercentualDesconto(double percentualDesconto) {
    this.percentualDesconto = percentualDesconto;
  }

  public LocalDate getDataAdesao() {
    return dataAdesao;
  }
}
