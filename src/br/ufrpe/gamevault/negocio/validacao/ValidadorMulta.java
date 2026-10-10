package br.ufrpe.gamevault.negocio.validacao;

import br.ufrpe.gamevault.negocio.beans.Aluguel;
import br.ufrpe.gamevault.negocio.beans.Cliente;
import br.ufrpe.gamevault.negocio.beans.Jogo;
import br.ufrpe.gamevault.negocio.validacao.excecoes.ValidacaoException;

import java.util.List;

public class ValidadorMulta implements IValidadorLocacao {
    @Override
    public void validar(Cliente cliente, Jogo jogo, List<Aluguel> alugueisAtivos) throws ValidacaoException {
        if(cliente.getPenalidades() != null){
            throw new ValidacaoException("Locação bloqueada: cliente possui multa pendente.");
        }
    }
}
