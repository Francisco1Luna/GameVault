package negocio;

import dados.IRepositorioPlataformas;
import model.Plataforma;

import java.util.List;
import java.util.Optional;

public class PlataformaService {

    private final IRepositorioPlataformas repositorioPlataformas;

    public PlataformaService(IRepositorioPlataformas repositorioPlataformas) {
        if (repositorioPlataformas == null) {
            throw new IllegalArgumentException("O repósitorio de plataformas não pode ser vazio.");
        }
        this.repositorioPlataformas = repositorioPlataformas;
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
