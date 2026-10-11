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
        // cliente -> aluguéis -> penalidade de cada aluguel
        if (cliente.getAlugueis() != null) {
            for (Aluguel aluguel : cliente.getAlugueis()) {
                Penalidade penalidade = aluguel.getPenalidade();
                if (penalidade != null && !penalidade.isPaga()) {
                    throw new ValidacaoException("Locação bloqueada: cliente possui multa pendente.");
                }
            }
        }
    }
}
