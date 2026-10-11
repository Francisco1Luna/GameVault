package negocio.validacao;

import model.Aluguel;
import model.Cliente;
import model.Jogo;
import negocio.excecoes.ValidacaoException;

import java.util.List;

public class ValidadorLimiteLocacoesAtivas implements IValidadorLocacao {
    @Override
    public void validar(Cliente cliente, Jogo jogo, List<Aluguel> alugueisAtivos) throws ValidacaoException {
        // o limite vem do próprio cliente (padrão = 1, assinante = 3, ajustável por cliente)
        int limite = cliente.getLimiteAluguelSimultaneo();

        if (alugueisAtivos.size() >= limite) {
            throw new ValidacaoException("Cliente atingiu o limite de locações ativas: " + limite);
        }
    }
}
