package negocio.validacao;

import model.Cliente;
import model.Jogo;
import model.Aluguel;
import negocio.excecoes.ValidacaoException;

import java.util.List;

public interface IValidadorLocacao {
    void validar(Cliente cliente, Jogo jogo, List<Aluguel> alugueisAtivos) throws ValidacaoException;
}
