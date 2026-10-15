package negocio;

import dados.IRepositorioJogos;
import model.Jogo;

import java.util.List;
import java.util.Optional;

public class JogoService {

    private final IRepositorioJogos repositorioJogos;

    public JogoService(IRepositorioJogos repositorioJogos) {
        if (repositorioJogos == null) {
            throw new IllegalArgumentException("O repositorio de jogo não pode ser nulo.");
        }
        this.repositorioJogos = repositorioJogos;
    }

    public Jogo salvar(Jogo jogo) {
        if (jogo == null) {
            throw new IllegalArgumentException("O jogo não pode ser vazio e deve possuir id.");
        }
        if (jogo.getTitulo() == null || jogo.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O jogo não pode ser vazaio e deve possuir um título.");
        }
       return repositorioJogos.salvar(jogo);

    }

    public void atualizar(Jogo jogo) {
         if (jogo == null || jogo.getId() == null) {
            throw new IllegalArgumentException("O jogo não pode ser vazio e deve possuir id.");
        }
        if (jogo == null || jogo.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O jogo não pode ser vazaio e deve possuir um título.");
        }
        repositorioJogos.atualizar(jogo);
    }

    public Optional<Jogo> buscarPorId(Long id) {
        if (id == null) {
           return Optional.empty();
        }

        return repositorioJogos.buscarPorId(id);
    }

    public List<Jogo> listar (){
        return repositorioJogos.listar();
    }
}
