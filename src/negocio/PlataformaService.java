package negocio;

import dados.IRepositorioJogos;
import dados.IRepositorioPlataformas;
import model.Jogo;
import model.Plataforma;

import java.util.List;
import java.util.Optional;

public class PlataformaService {

    private final IRepositorioPlataformas repositorioPlataformas;
    private final IRepositorioJogos repositorioJogos;

    public PlataformaService(IRepositorioPlataformas repositorioPlataformas, IRepositorioJogos repositorioJogos) {
        if (repositorioPlataformas == null) {
            throw new IllegalArgumentException("O repósitorio de plataformas não pode ser vazio.");
        }
        if (repositorioJogos == null) {
            throw new IllegalArgumentException("O repositório de jogos é obrigatório para verificar o uso da plataforma.");
        }
        this.repositorioPlataformas = repositorioPlataformas;
        this.repositorioJogos = repositorioJogos;
    }

    public Plataforma cadastrar(Plataforma plataforma) {
        if (plataforma == null) {
            throw new IllegalArgumentException("A plataforma é obrigatória.");
        }
        if (plataforma.getNome() == null || plataforma.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da plataforma é obrigatório.");
        }
        return repositorioPlataformas.salvar(plataforma);
    }

    public void atualizar(Plataforma plataforma) {
        if (plataforma == null || plataforma.getId() == null) {
            throw new IllegalArgumentException("Plataforma inválida para atualização.");
        }
        if (plataforma.getNome() == null || plataforma.getNome().trim().isEmpty()) {
           throw new IllegalArgumentException("O nome da plataforma não pode ser vazio");
        }
        repositorioPlataformas.atualizar(plataforma);
    }

    /**
     * Remove a plataforma. Não deixa remover se algum jogo ainda a usa, senão o jogo ficaria
     * apontando para uma plataforma que não existe mais. Devolve false se o id não existe.
     */
    public synchronized boolean remover(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("O id da plataforma é obrigatório.");
        }
        Optional<Plataforma> plataforma = repositorioPlataformas.buscarPorId(id);
        if (!plataforma.isPresent()) {
            return false;
        }
        for (Jogo jogo : repositorioJogos.listar()) {
            if (jogo.getPlataformas() == null) {
                continue;
            }
            for (Plataforma usada : jogo.getPlataformas()) {
                if (usada != null && id.equals(usada.getId())) {
                    throw new IllegalStateException("Não é possível remover a plataforma \"" + plataforma.get().getNome()
                            + "\": o jogo \"" + jogo.getTitulo() + "\" ainda a utiliza.");
                }
            }
        }
        return repositorioPlataformas.remover(id);
    }

    public Optional<Plataforma> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return repositorioPlataformas.buscarPorId(id);
    }

    public List<Plataforma> listar() {
        return repositorioPlataformas.listar();
    }
}
