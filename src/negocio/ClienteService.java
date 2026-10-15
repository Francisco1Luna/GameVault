package negocio;

import dados.IRepositorioClientes;
import model.Cliente;

import java.lang.ref.Cleaner;
import java.util.List;
import java.util.Optional;

public class ClienteService {

    private final IRepositorioClientes repositorioClientes;


    public ClienteService(IRepositorioClientes repositorioClientes) {
        if (repositorioClientes == null) {
            throw new IllegalArgumentException("O repositório de clientes deve existir.");
        }
        this.repositorioClientes = repositorioClientes;
    }

    public Cliente salvar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("O cliente não pode ser nulo.");
        }
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }
        if (cliente.getCpf() == null || cliente.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("O CPF do cliente é obrigatório.");
        }
        return repositorioClientes.salvar(cliente);
    }

    public void atualizar(Cliente cliente) {
        if (cliente == null || cliente.getId() == null) {
            throw new IllegalArgumentException("Cliente inválido para atualização.");
        }
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do cliente não pode ser vazio.");
        }
        if (cliente.getCpf() == null || cliente.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("O CPF do cliente não pode ser vazio.");
        }
        repositorioClientes.atualizar(cliente);
    }

    public Optional<Cliente> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return repositorioClientes.buscarPorId(id);
    }

    public List<Cliente> listar() {
        return repositorioClientes.listar();
    }
}
