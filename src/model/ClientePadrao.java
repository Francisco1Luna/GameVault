package model;

import java.time.LocalDate;

public class ClientePadrao extends Cliente {

  private static final int LIMITE_ALUGUEL_SIMULTANEO = 1;

  public ClientePadrao(Long id, String nome, String cpf, LocalDate dataNascimento, String email, String telefone) {
    super(id, nome, cpf, dataNascimento, email, telefone, LIMITE_ALUGUEL_SIMULTANEO);
  }
}
