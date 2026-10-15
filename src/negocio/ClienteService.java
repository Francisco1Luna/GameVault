package negocio;

import dados.IRepositorioClientes;
import model.Aluguel;
import model.Cliente;
import model.enums.StatusAluguel;

import java.util.List;
import java.util.Optional;

public class ClienteService {

    private final IRepositorioClientes repositorioClientes;
    private final FinanceiroService financeiroService;

    public ClienteService(IRepositorioClientes repositorioClientes) {
        this(repositorioClientes, new FinanceiroService());
    }

    public ClienteService(IRepositorioClientes repositorioClientes, FinanceiroService financeiroService) {
        if (repositorioClientes == null) {
            throw new IllegalArgumentException("O repositório de clientes deve existir.");
        }
        if (financeiroService == null) {
            throw new IllegalArgumentException("O serviço financeiro é obrigatório.");
        }
        this.repositorioClientes = repositorioClientes;
        this.financeiroService = financeiroService;
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

    /**
     * Remove o cliente. Não deixa remover se ele tem aluguel em andamento ou multa não paga.
     * Devolve false se o id não existe.
     */
    public synchronized boolean remover(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("O id do cliente é obrigatório.");
        }
        Optional<Cliente> encontrado = repositorioClientes.buscarPorId(id);
        if (!encontrado.isPresent()) {
            return false;
        }
        Cliente cliente = encontrado.get();
        if (cliente.getAlugueis() != null) {
            for (Aluguel aluguel : cliente.getAlugueis()) {
                if (aluguel != null && aluguel.getStatus() != StatusAluguel.DEVOLVIDO) {
                    throw new IllegalStateException("Não é possível remover o cliente \"" + cliente.getNome()
                            + "\": ele possui aluguel em andamento.");
                }
            }
        }
        if (financeiroService.possuiPendencias(cliente)) {
            throw new IllegalStateException("Não é possível remover o cliente \"" + cliente.getNome()
                    + "\": ele possui multa pendente.");
        }
        return repositorioClientes.remover(id);
    }

    public Optional<Cliente> buscarPorCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return Optional.empty();
        }
        return repositorioClientes.buscarPorCpf(cpf.trim());
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
