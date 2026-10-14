package dados;

import java.util.List;
import java.util.Optional;

import model.Venda;

public interface IRepositorioVendas {

  Venda salvar(Venda venda);

  void atualizar(Venda venda);

  Optional<Venda> buscarPorId(Long id);

  List<Venda> listar();
}
