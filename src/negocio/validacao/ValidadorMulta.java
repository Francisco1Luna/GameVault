package negocio.validacao;

import model.Aluguel;
import model.Cliente;
import model.Jogo;
import negocio.FinanceiroService;
import negocio.excecoes.ValidacaoException;

import java.util.List;

public class ValidadorMulta implements IValidadorLocacao {
    @Override
    public void validar(Cliente cliente, Jogo jogo, List<Aluguel> alugueisAtivos) throws ValidacaoException {
        // inclui a multa que já corre em aluguel atrasado ainda não devolvido
        if (FinanceiroService.getInstance().possuiPendencias(cliente)) {
            throw new ValidacaoException("Locação bloqueada: cliente possui multa pendente.");
        }
    }
}
