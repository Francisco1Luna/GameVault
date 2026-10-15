package dados;

import model.Plataforma;

import java.util.List;
import java.util.Optional;

public interface IRepositorioPlataformas {

    Plataforma salvar(Plataforma plataforma);

    void atualizar(Plataforma plataforma);

    Optional<Plataforma> buscarPorId(Long id);

    List<Plataforma> listar();

}