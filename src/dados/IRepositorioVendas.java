package dados;

import java.util.List;
import java.util.Optional;

import model.Cliente;
import model.Venda;


public interface IRepositorioVendas {

  Venda salvar(Venda venda);

  Optional<Venda> buscarPorId(Long id);

  List<Venda> listarPorCliente(Cliente cliente);

  List<Venda> listar();
}
