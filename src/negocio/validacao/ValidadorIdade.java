package negocio.validacao;

import model.Aluguel;
import model.Cliente;
import model.Jogo;
import negocio.excecoes.ValidacaoException;

import java.util.List;

public class ValidadorIdade implements IValidadorLocacao{
    @Override
    public void validar(Cliente cliente, Jogo jogo, List<Aluguel> alugueisAtivos) throws ValidacaoException {
        int idade = cliente.getIdade();

        if (idade < 1){
            throw new ValidacaoException("Cliente não possui a idade mínima para este jogo.");
        }
    }
}
