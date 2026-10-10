package negocio.validacao;

import model.Aluguel;
import model.Cliente;
import model.Jogo;
import negocio.excecoes.ValidacaoException;

import java.util.List;

public class ValidadorMulta implements IValidadorLocacao {
    @Override
    public void validar(Cliente cliente, Jogo jogo, List<Aluguel> alugueisAtivos) throws ValidacaoException {
        if(cliente.getPenalidades() != null){
            throw new ValidacaoException("Locação bloqueada: cliente possui multa pendente.");
        }
    }
}
