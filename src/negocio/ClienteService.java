package negocio;

import dados.RepositorioClientesLista;
import dados.IRepositorioClientes;
import model.Aluguel;
import model.Cliente;
import model.ClienteAssinante;
import model.ClientePadrao;
import model.enums.StatusAluguel;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ClienteService {

    // Singleton: uma única instância em memória; use ClienteService.getInstance().
    private static final ClienteService INSTANCIA = new ClienteService(
        RepositorioClientesLista.getInstance(), FinanceiroService.getInstance());

    public static ClienteService getInstance() {
        return INSTANCIA;
    }

    private final IRepositorioClientes repositorioClientes;
    private final FinanceiroService financeiroService;

    private ClienteService(IRepositorioClientes repositorioClientes, FinanceiroService financeiroService) {
        if (repositorioClientes == null) {
            throw new IllegalArgumentException("O repositório de clientes deve existir.");
        }
        if (financeiroService == null) {
            throw new IllegalArgumentException("O serviço financeiro é obrigatório.");
        }
        this.repositorioClientes = repositorioClientes;
        this.financeiroService = financeiroService;
    }

    /**
     * Cadastra o cliente. O CPF é guardado só com dígitos (111.222.333-44 e 11122233344 são o
     * mesmo CPF) e precisa ter os dígitos verificadores corretos. Criar um cliente administrador
     * exige administrador logado (ou carga do sistema).
     */
    public synchronized Cliente salvar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("O cliente não pode ser nulo.");
        }
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }
        if (cliente.isAdministrador()) {
            Sessao.exigirAdministrador();
        }
        String cpf = Cpf.normalizar(cliente.getCpf());
        validarCpf(cpf, cliente.isAdministrador());
        if (repositorioClientes.buscarPorCpf(cpf).isPresent()) {
            throw new IllegalStateException("Já existe um cliente com o CPF " + cpf + ".");
        }
        cliente.setCpf(cpf);
        return repositorioClientes.salvar(cliente);
    }

    /**
     * Edita os dados de um cliente (só administrador). Valida tudo antes de alterar qualquer coisa:
     * se algo for inválido nada muda. Quando {@code assinante} difere do tipo atual o cliente
     * muda de tipo (Padrão ou Assinante), mantendo id, senha, aluguéis e o resto.
     * Devolve o cliente resultante, que pode ser outro objeto se o tipo mudou.
     */
    public synchronized Cliente editar(Long id, String nome, String cpf, LocalDate dataNascimento,
                                       String email, String telefone, boolean assinante) {
        Sessao.exigirAdministrador();
        if (id == null) {
            throw new IllegalArgumentException("Cliente inválido para atualização.");
        }
        Cliente atual = repositorioClientes.buscarPorId(id).orElseThrow(
            () -> new IllegalArgumentException("Cliente não encontrado: id " + id + "."));
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do cliente não pode ser vazio.");
        }
        String cpfNormalizado = Cpf.normalizar(cpf);
        validarCpf(cpfNormalizado, atual.isAdministrador());
        if (dataNascimento == null) {
            throw new IllegalArgumentException("A data de nascimento é obrigatória.");
        }
        if (dataNascimento.isAfter(Relogio.hoje())) {
            throw new IllegalArgumentException("A data de nascimento não pode ser futura.");
        }
        Optional<Cliente> mesmoCpf = repositorioClientes.buscarPorCpf(cpfNormalizado);
        if (mesmoCpf.isPresent() && !mesmoCpf.get().getId().equals(id)) {
            throw new IllegalStateException("Já existe outro cliente com o CPF " + cpfNormalizado + ".");
        }

        // a partir daqui nada falha: pode alterar
        boolean eraAssinante = atual instanceof ClienteAssinante;
        Cliente resultado = atual;
        if (assinante != eraAssinante) {
            resultado = assinante
                ? new ClienteAssinante(id, nome, cpfNormalizado, dataNascimento, null, null,
                    AuthService.PERCENTUAL_DESCONTO_ASSINANTE, Relogio.hoje())
                : new ClientePadrao(id, nome, cpfNormalizado, dataNascimento, null, null);
            resultado.setAdministrador(atual.isAdministrador());
            resultado.setSenhaHash(atual.getSenhaHash());
            resultado.setAlugueis(atual.getAlugueis());
            if (atual.getAlugueis() != null) {
                for (Aluguel aluguel : atual.getAlugueis()) {
                    if (aluguel != null) {
                        aluguel.setCliente(resultado); // o histórico passa a apontar para o novo objeto
                    }
                }
            }
        }
        resultado.setNome(nome.trim());
        resultado.setCpf(cpfNormalizado);
        resultado.setDataNascimento(dataNascimento);
        resultado.setEmail(vazioParaNulo(email));
        resultado.setTelefone(vazioParaNulo(telefone));
        if (resultado != atual) {
            repositorioClientes.atualizar(resultado);
        }
        return resultado;
    }

    /**
     * Remove o cliente (só administrador). Não deixa remover a si mesmo, nem quem tem aluguel em
     * andamento ou multa não paga. Devolve false se o id não existe.
     */
    public synchronized boolean remover(Long id) {
        Sessao.exigirAdministrador();
        if (id == null) {
            throw new IllegalArgumentException("O id do cliente é obrigatório.");
        }
        Optional<Cliente> encontrado = repositorioClientes.buscarPorId(id);
        if (!encontrado.isPresent()) {
            return false;
        }
        Cliente cliente = encontrado.get();
        if (id.equals(Sessao.idUsuarioAtual())) {
            throw new IllegalStateException("Você não pode remover o usuário com o qual está logado.");
        }
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

    /** Busca pelo CPF com ou sem máscara. */
    public Optional<Cliente> buscarPorCpf(String cpf) {
        String normalizado = Cpf.normalizar(cpf);
        if (normalizado.isEmpty()) {
            return Optional.empty();
        }
        return repositorioClientes.buscarPorCpf(normalizado);
    }

    public Optional<Cliente> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return repositorioClientes.buscarPorId(id);
    }

    /** Lista todos os clientes (só administrador: os dados incluem CPF e hash de senha). */
    public List<Cliente> listar() {
        Sessao.exigirAdministrador();
        return repositorioClientes.listar();
    }

    /** CPF já normalizado: 11 dígitos com dígitos verificadores corretos (exceto o reservado do administrador). */
    private static void validarCpf(String cpf, boolean administrador) {
        if (cpf.isEmpty()) {
            throw new IllegalArgumentException("O CPF do cliente é obrigatório.");
        }
        if (cpf.length() != 11) {
            throw new IllegalArgumentException("O CPF deve ter 11 dígitos.");
        }
        if (administrador && Cpf.RESERVADO_ADMINISTRADOR.equals(cpf)) {
            return;
        }
        if (!Cpf.ehValido(cpf)) {
            throw new IllegalArgumentException("CPF inválido: confira os dígitos.");
        }
    }

    private static String vazioParaNulo(String texto) {
        return texto == null || texto.trim().isEmpty() ? null : texto.trim();
    }
}
