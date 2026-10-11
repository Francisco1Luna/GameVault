package negocio.validacao;

import model.Aluguel;
import model.Cliente;
import model.Jogo;
import model.Penalidade;
import negocio.excecoes.ValidacaoException;

import java.util.List;

public class ValidadorMulta implements IValidadorLocacao {
    @Override
    public void validar(Cliente cliente, Jogo jogo, List<Aluguel> alugueisAtivos) throws ValidacaoException {
        if (cliente.getPenalidades() != null) {
            for (Penalidade penalidade : cliente.getPenalidades()) {
                if (!penalidade.isPaga()) {
                    throw new ValidacaoException("Locação bloqueada: cliente possui multa pendente.");
                }
            }
        }
    }
}
