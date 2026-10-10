package br.ufrpe.gamevault.negocio.validacao;

import br.ufrpe.gamevault.negocio.beans.Aluguel;
import br.ufrpe.gamevault.negocio.beans.Cliente;
import br.ufrpe.gamevault.negocio.beans.ClienteAssinante;
import br.ufrpe.gamevault.negocio.beans.Jogo;
import br.ufrpe.gamevault.negocio.validacao.excecoes.ValidacaoException;

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
