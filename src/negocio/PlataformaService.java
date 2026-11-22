package negocio;

import dados.RepositorioJogosLista;
import dados.RepositoriosPlataformas;
import dados.IRepositorioJogos;
import dados.IRepositorioPlataformas;
import model.Jogo;
import model.Plataforma;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

/** Cadastro de plataformas. Criar, editar e remover é restrito ao administrador (ver {@link Sessao}). */
public class PlataformaService {

    // Singleton: uma única instância em memória; use PlataformaService.getInstance().
    private static final PlataformaService INSTANCIA = new PlataformaService(
        RepositoriosPlataformas.getInstance(), RepositorioJogosLista.getInstance());

    public static PlataformaService getInstance() {
        return INSTANCIA;
    }

    private final IRepositorioPlataformas repositorioPlataformas;
    private final IRepositorioJogos repositorioJogos;

    private PlataformaService(IRepositorioPlataformas repositorioPlataformas, IRepositorioJogos repositorioJogos) {
        if (repositorioPlataformas == null) {
            throw new IllegalArgumentException("O repósitorio de plataformas não pode ser vazio.");
        }
        if (repositorioJogos == null) {
            throw new IllegalArgumentException("O repositório de jogos é obrigatório para verificar o uso da plataforma.");
        }
        this.repositorioPlataformas = repositorioPlataformas;
        this.repositorioJogos = repositorioJogos;
    }

    public synchronized Plataforma cadastrar(Plataforma plataforma) {
        Sessao.exigirAdministrador();
        validar(plataforma, "A plataforma é obrigatória.", "O nome da plataforma é obrigatório.");
        return repositorioPlataformas.salvar(plataforma);
    }

    /**
     * Devolve a plataforma com esse nome (sem diferenciar maiúsculas e acentos) ou a cadastra se
     * ainda não existir. É uma operação só, sob o mesmo bloqueio, para duas importações ao mesmo
     * tempo não criarem a mesma plataforma duas vezes.
     */
    public synchronized Plataforma obterOuCadastrar(String nome, String fabricante) {
        Sessao.exigirAdministrador();
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da plataforma é obrigatório.");
        }
        String chave = normalizar(nome);
        for (Plataforma existente : repositorioPlataformas.listar()) {
            if (existente.getNome() != null && normalizar(existente.getNome()).equals(chave)) {
                return existente;
            }
        }
        return cadastrar(new Plataforma(null, nome.trim(), fabricante));
    }

    /**
     * Atualiza nome e fabricante da plataforma de mesmo id. O objeto recebido só carrega os novos
     * valores: eles são validados primeiro e só então copiados para a plataforma guardada (a mesma
     * que os jogos referenciam, então todos enxergam o novo nome). Se algo é inválido, nada muda.
     */
    public synchronized void atualizar(Plataforma plataforma) {
        Sessao.exigirAdministrador();
        if (plataforma == null || plataforma.getId() == null) {
            throw new IllegalArgumentException("Plataforma inválida para atualização.");
        }
        validar(plataforma, "Plataforma inválida para atualização.", "O nome da plataforma não pode ser vazio");
        Plataforma guardada = repositorioPlataformas.buscarPorId(plataforma.getId()).orElseThrow(
            () -> new IllegalArgumentException("Plataforma não encontrada: id " + plataforma.getId() + "."));
        guardada.setNome(plataforma.getNome().trim());
        guardada.setFabricante(plataforma.getFabricante().trim());
        repositorioPlataformas.atualizar(guardada);
    }

    /**
     * Remove a plataforma. Não deixa remover se algum jogo ainda a usa, senão o jogo ficaria
     * apontando para uma plataforma que não existe mais. Devolve false se o id não existe.
     */
    public synchronized boolean remover(Long id) {
        Sessao.exigirAdministrador();
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

    private static void validar(Plataforma plataforma, String mensagemNula, String mensagemNome) {
        if (plataforma == null) {
            throw new IllegalArgumentException(mensagemNula);
        }
        if (plataforma.getNome() == null || plataforma.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException(mensagemNome);
        }
        if (plataforma.getFabricante() == null || plataforma.getFabricante().trim().isEmpty()) {
            throw new IllegalArgumentException("O Fabricante da plataforma é obrigatório.");
        }
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
    }
}
