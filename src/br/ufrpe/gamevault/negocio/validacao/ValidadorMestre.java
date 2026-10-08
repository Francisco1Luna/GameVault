package br.ufrpe.gamevault.negocio.validacao;

import br.ufrpe.gamevault.negocio.beans.Aluguel;
import br.ufrpe.gamevault.negocio.beans.Cliente;
import br.ufrpe.gamevault.negocio.beans.Jogo;
import br.ufrpe.gamevault.negocio.validacao.excecoes.ValidacaoException;

import java.util.ArrayList;
import java.util.List;

public class ValidadorMestre implements IValidadorLocacao{
    private final List<IValidadorLocacao> validadores;

    public ValidadorMestre(){
        this.validadores = new ArrayList<>();

        this.validadores.add(new ValidadorIdade());
        this.validadores.add(new ValidadorMulta());
        this.validadores.add(new ValidadorLimiteLocacoesAtivas());
        this.validadores.add(new ValidadorEstoque());
    }

    @Override
    public void validar(Cliente cliente, Jogo jogo, List<Aluguel> alugueisAtivos) throws ValidacaoException{
        for(IValidadorLocacao validador : validadores) {
            validador.validar(cliente, jogo, alugueisAtivos);
        }
    }
}
