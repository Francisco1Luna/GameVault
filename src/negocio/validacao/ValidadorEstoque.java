package negocio.validacao;

import model.Aluguel;
import model.Cliente;
import model.Jogo;
import negocio.excecoes.ValidacaoException;

import java.util.List;

public class ValidadorEstoque implements IValidadorLocacao{
    @Override
    public void validar(Cliente cliente, Jogo jogo, List<Aluguel> alugueisAtivos) throws ValidacaoException {
        if (jogo.getEstoquePorPlataforma().size() <= 0){
            throw new ValidacaoException("Licença indisponível no estoque.");
        }
    }
}
