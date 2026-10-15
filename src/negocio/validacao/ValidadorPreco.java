package negocio.validacao;

import model.Aluguel;
import model.Cliente;
import model.Jogo;
import negocio.excecoes.ValidacaoException;

import java.util.List;

public class ValidadorPreco implements IValidadorLocacao {
    @Override
    public void validar(Cliente cliente, Jogo jogo, List<Aluguel> alugueisAtivos) throws ValidacaoException {
        if (jogo.getPrecoLocacaoDiaria() == null || jogo.getPrecoLocacaoDiaria() <= 0) {
            throw new ValidacaoException("Jogo sem preço de locação definido: " + jogo.getTitulo());
        }
    }
}
