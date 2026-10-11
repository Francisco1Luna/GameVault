package negocio.validacao;

import model.Aluguel;
import model.Cliente;
import model.Jogo;
import negocio.excecoes.ValidacaoException;

import java.util.List;

public class ValidadorEstoque implements IValidadorLocacao{
    @Override
    public void validar(Cliente cliente, Jogo jogo, List<Aluguel> alugueisAtivos) throws ValidacaoException {
        if (jogo.getEstoquePorPlataforma() == null
                || jogo.getEstoquePorPlataforma().values().stream().noneMatch(quantidade -> quantidade != null && quantidade > 0)) {
            throw new ValidacaoException("Licença indisponível no estoque.");
        }
    }
}
