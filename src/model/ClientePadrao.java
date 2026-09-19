package model;

import java.time.LocalDate;

public class ClientePadrao extends Cliente {

  private static final int LIMITE_ALUGUEL_SIMULTANEO = 1;

  public ClientePadrao(Long id, String nome, String cpf, LocalDate dataNascimento, String email, String telefone) {
    super(id, nome, cpf, dataNascimento, email, telefone);
  }

  @Override
  public int getLimiteAluguelSimultaneo() {
    return LIMITE_ALUGUEL_SIMULTANEO;
  }

  @Override
  public double aplicarDesconto(double valorBruto) {
    // Cliente padrão não tem desconto — devolve o valor cheio.
    return valorBruto;
  }
}
