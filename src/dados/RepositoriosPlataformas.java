package dados;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Plataforma;

public class RepositoriosPlataformas implements IRepositorioPlataformas {

    private final List<Plataforma> plataformas = new ArrayList<>();
    private long proximoId = 1;

    @Override
    public synchronized Plataforma salvar(Plataforma plataforma) {
        if (plataforma == null) {
            throw new IllegalArgumentException("A plataforma é obrigatória.");
        } if (plataforma.getId() == null) {
            plataforma.setId(proximoId++);
        } else {
            if (buscarPorId(plataforma.getId()).isPresent()) {
                throw new IllegalArgumentException("Já existe uma plataforma com o id" + plataforma.getId() + ".");
            } proximoId = Math.max(proximoId, plataforma.getId() + 1);
        }
        plataformas.add(plataforma);
        return plataforma;
    }

    @Override
    public synchronized void atualizar(Plataforma plataforma) {
        if (plataforma == null || plataforma.getId() == null) {
            throw new IllegalArgumentException("So é possivel atualizar uma plataforma já cadastrada");
        }
        for (int i = 0; i < plataformas.size();i++) {
            if (plataformas.get(i).getId().equals(plataforma.getId())) {
                plataformas.set(i,plataforma);
                return;
            }
        }
        throw new IllegalArgumentException("Plataforma não encontrada: id " + plataforma.getId() + ".");
    }

    @Override
    public synchronized boolean remover(Long id) {
        for (int i = 0; i < plataformas.size(); i++) {
            if (plataformas.get(i).getId() != null && plataformas.get(i).getId().equals(id)) {
                plataformas.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized Optional<Plataforma> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        for (Plataforma plataforma : plataformas) {
            if (plataforma.getId() != null && plataforma.getId().equals(id)) {
                return Optional.of(plataforma);
            }
        }
        return Optional.empty();

    }

    @Override
    public synchronized List<Plataforma> listar() {
        return new ArrayList<>(plataformas);
    }
}
