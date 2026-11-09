package model.enums;

public enum ClassificacaoIndicativa {
  LIVRE("Livre"),
  DEZ_ANOS("10 anos"),
  DOZE_ANOS("12 anos"),
  QUATORZE_ANOS("14 anos"),
  DEZESSEIS_ANOS("16 anos"),
  DEZOITO_ANOS("18 anos");

  private final String rotulo;

  ClassificacaoIndicativa(String rotulo) {
    this.rotulo = rotulo;
  }

  /** Texto mostrado na tela (o nome da constante continua sendo usado no código). */
  @Override
  public String toString() {
    return rotulo;
  }
}