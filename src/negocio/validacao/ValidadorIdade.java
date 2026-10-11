package negocio.validacao;

import model.Aluguel;
import model.Cliente;
import model.Jogo;
import negocio.excecoes.ValidacaoException;

import java.util.List;

public class ValidadorIdade implements IValidadorLocacao{
    @Override
    public void validar(Cliente cliente, Jogo jogo, List<Aluguel> alugueisAtivos) throws ValidacaoException {
        if (cliente.getDataNascimento() == null) {
            throw new ValidacaoException("Cliente não possui data de nascimento.");
        }
        if (jogo.getClassificacaoIndicativa() == null) {
            throw new ValidacaoException("Jogo não possui classificação indicativa.");
        }

        int idade = cliente.getIdade();
        int idadeMinima;
        switch (jogo.getClassificacaoIndicativa()) {
            case LIVRE:
                idadeMinima = 0;
                break;
            case DEZ_ANOS:
                idadeMinima = 10;
                break;
            case DOZE_ANOS:
                idadeMinima = 12;
                break;
            case QUATORZE_ANOS:
                idadeMinima = 14;
                break;
            case DEZESSEIS_ANOS:
                idadeMinima = 16;
                break;
            case DEZOITO_ANOS:
                idadeMinima = 18;
                break;
            default:
                throw new ValidacaoException("Classificação indicativa inválida.");
        }

        if (idade < idadeMinima) {
            throw new ValidacaoException("Cliente não possui idade mínima para este jogo.");
        }
    }
}
