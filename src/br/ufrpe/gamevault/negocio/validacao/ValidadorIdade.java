package br.ufrpe.gamevault.negocio.validacao;

import br.ufrpe.gamevault.negocio.beans.Aluguel;
import br.ufrpe.gamevault.negocio.beans.Cliente;
import br.ufrpe.gamevault.negocio.beans.Jogo;
import br.ufrpe.gamevault.negocio.validacao.excecoes.ValidacaoException;

import java.util.List;

public class ValidadorIdade implements IValidadorLocacao{
    @Override
    public void validar(Cliente cliente, Jogo jogo, List<Aluguel> alugueisAtivos) throws ValidacaoException {
        int idade = cliente.getIdade();

        if (idade < 1){
            throw new ValidacaoException("Cliente não possui a idade mínima para este jogo.");
        }
    }
}
