package negocio.validacao;

import model.Aluguel;
import model.Cliente;
import model.ClienteAssinante;
import model.Jogo;
import negocio.excecoes.ValidacaoException;

import java.util.List;

public class ValidadorLimiteLocacoesAtivas implements IValidadorLocacao {
    @Override
    public void validar(Cliente cliente, Jogo jogo, List<Aluguel> alugueisAtivos) throws ValidacaoException {
        int limite;
        if (cliente instanceof ClienteAssinante) {
            limite = 3;
        } else {
            limite = 1;
        }

        if (alugueisAtivos.size() >= limite) {
            throw new ValidacaoException("Cliente atingiu o valor de locações ativas: "+ limite);
        }
    }
}
