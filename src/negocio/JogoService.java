package negocio;

import dados.RepositorioAlugueisLista;
import dados.RepositorioJogosLista;
import dados.IRepositorioAlugueis;
import dados.IRepositorioJogos;
import model.Aluguel;
import model.Jogo;
import model.Plataforma;
import model.enums.StatusAluguel;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class JogoService {

    // Singleton: uma única instância em memória; use JogoService.getInstance().
    private static final JogoService INSTANCIA = new JogoService(
        RepositorioJogosLista.getInstance(), RepositorioAlugueisLista.getInstance(),
        PlataformaService.getInstance());

    public static JogoService getInstance() {
        return INSTANCIA;
    }

    private final IRepositorioJogos repositorioJogos;
    private final IRepositorioAlugueis repositorioAlugueis;
    private final PlataformaService plataformaService;

    private JogoService(IRepositorioJogos repositorioJogos, IRepositorioAlugueis repositorioAlugueis,
                        PlataformaService plataformaService) {
        if (repositorioJogos == null) {
            throw new IllegalArgumentException("O repositorio de jogo não pode ser nulo.");
        }
        if (repositorioAlugueis == null) {
            throw new IllegalArgumentException("O repositório de aluguéis é obrigatório para verificar o uso do jogo.");
        }
        if (plataformaService == null) {
            throw new IllegalArgumentException("O serviço de plataformas é obrigatório.");
        }
        this.repositorioJogos = repositorioJogos;
        this.repositorioAlugueis = repositorioAlugueis;
        this.plataformaService = plataformaService;
    }

    /** Cadastra um jogo (só administrador). Exige preço, locação e classificação indicativa válidos. */
    public synchronized Jogo salvar(Jogo jogo) {
        Sessao.exigirAdministrador();
        if (jogo == null) {
            throw new IllegalArgumentException("O jogo é obrigatório.");
        }
        validar(jogo);
        return repositorioJogos.salvar(jogo);
    }

    /**
     * Atualiza o jogo de mesmo id (só administrador). O objeto recebido só carrega os novos
     * valores: eles são validados primeiro e só então copiados para o jogo guardado, então se algo
     * for inválido nada muda e quem chama não precisa desfazer nada. Não deixa tirar uma
     * plataforma que tem locação em andamento. O estoque das plataformas retiradas é descartado.
     */
    public synchronized void atualizar(Jogo novo) {
        Sessao.exigirAdministrador();
        if (novo == null || novo.getId() == null) {
            throw new IllegalArgumentException("O jogo não pode ser vazio e deve possuir id.");
        }
        Jogo guardado = repositorioJogos.buscarPorId(novo.getId()).orElseThrow(
            () -> new IllegalArgumentException("Jogo não encontrado: id " + novo.getId() + "."));
        validar(novo);

        List<Plataforma> plataformas = novo.getPlataformas() == null
            ? new ArrayList<>() : new ArrayList<>(novo.getPlataformas());
        exigirSemLocacaoNasPlataformasRemovidas(guardado, plataformas);

        // validado: agora sim copia (se for o mesmo objeto, só ajusta plataformas e estoque)
        if (novo != guardado) {
            guardado.setTitulo(novo.getTitulo().trim());
            guardado.setGenero(novo.getGenero().trim());
            guardado.setClassificacaoIndicativa(novo.getClassificacaoIndicativa());
            guardado.setPreco(novo.getPreco());
            guardado.setPrecoLocacaoDiaria(novo.getPrecoLocacaoDiaria());
            guardado.setDesenvolvedora(novo.getDesenvolvedora());
            guardado.setPublicadora(novo.getPublicadora());
            guardado.setUrlReferencia(novo.getUrlReferencia());
            guardado.setUrlThumbnail(novo.getUrlThumbnail());
        }
        guardado.setPlataformas(plataformas);
        if (guardado.getEstoquePorPlataforma() != null) {
            guardado.getEstoquePorPlataforma().keySet().removeIf(chave -> !contem(plataformas, chave));
        }
        repositorioJogos.atualizar(guardado);
    }

    /**
     * Importa o catálogo da FreeToGame (só administrador). Os jogos entram incompletos de
     * propósito: sem preço, sem locação, sem classificação e sem estoque, porque a API não traz
     * esses dados. Enquanto o administrador não completar o cadastro (ver {@link #estaCompleto}) o
     * jogo aparece na loja mas não pode ser comprado nem alugado. Ignora títulos que já existem.
     * É tudo ou nada: se algum jogo falhar, os já criados são removidos. Devolve quantos entraram.
     */
    public synchronized int importarDaApi(List<FreeToGameService.DadosJogo> dadosApi) {
        Sessao.exigirAdministrador();
        if (dadosApi == null) {
            throw new IllegalArgumentException("Não há dados para importar.");
        }
        Set<String> titulos = new HashSet<>();
        for (Jogo existente : repositorioJogos.listar()) {
            titulos.add(normalizar(existente.getTitulo() == null ? "" : existente.getTitulo()));
        }
        List<Jogo> criados = new ArrayList<>();
        try {
            for (FreeToGameService.DadosJogo dado : dadosApi) {
                if (dado.getTitulo() == null || dado.getTitulo().trim().isEmpty()
                        || !titulos.add(normalizar(dado.getTitulo()))) {
                    continue;
                }
                String nomePlataforma = dado.getPlataformas() == null || dado.getPlataformas().trim().isEmpty()
                    ? "Outras" : dado.getPlataformas().trim();
                Plataforma plataforma = plataformaService.obterOuCadastrar(nomePlataforma, "FreeToGame");
                String genero = dado.getGenero() == null || dado.getGenero().trim().isEmpty()
                    ? "Não informado" : dado.getGenero().trim();
                List<Plataforma> lista = new ArrayList<>();
                lista.add(plataforma);
                criados.add(repositorioJogos.salvar(new Jogo(null, dado.getTitulo().trim(), null,
                    dado.getDesenvolvedora(), genero, dado.getPublicadora(), null, null, lista,
                    new HashMap<>(), dado.getUrlThumbnail(), dado.getUrlReferencia())));
            }
        } catch (RuntimeException e) {
            for (Jogo criado : criados) {
                repositorioJogos.remover(criado.getId());
            }
            throw e;
        }
        return criados.size();
    }

    /** O jogo já pode ser vendido e alugado? (preço, locação e classificação preenchidos) */
    public boolean estaCompleto(Jogo jogo) {
        return jogo != null && jogo.getClassificacaoIndicativa() != null
            && jogo.getPreco() != null && jogo.getPreco() > 0
            && jogo.getPrecoLocacaoDiaria() != null && jogo.getPrecoLocacaoDiaria() > 0;
    }

    private void validar(Jogo jogo) {
        if (jogo.getTitulo() == null || jogo.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O título do jogo é obrigatório.");
        }
        if (jogo.getGenero() == null || jogo.getGenero().trim().isEmpty()) {
            throw new IllegalArgumentException("O gênero do jogo é obrigatório.");
        }
        if (jogo.getClassificacaoIndicativa() == null) {
            throw new IllegalArgumentException("A classificação indicativa do jogo é obrigatória.");
        }
        if (jogo.getPreco() == null || Double.isNaN(jogo.getPreco()) || jogo.getPreco() <= 0) {
            throw new IllegalArgumentException("O preço do jogo deve ser maior que zero.");
        }
        if (jogo.getPrecoLocacaoDiaria() == null || Double.isNaN(jogo.getPrecoLocacaoDiaria())
                || jogo.getPrecoLocacaoDiaria() <= 0) {
            throw new IllegalArgumentException("A locação por dia deve ser maior que zero.");
        }
    }

    /** Uma plataforma que o jogo perde não pode ter locação em andamento (a devolução ficaria sem estoque certo). */
    private void exigirSemLocacaoNasPlataformasRemovidas(Jogo guardado, List<Plataforma> novas) {
        if (guardado.getPlataformas() == null) {
            return;
        }
        for (Plataforma antiga : guardado.getPlataformas()) {
            if (antiga == null || contem(novas, antiga)) {
                continue;
            }
            for (Aluguel aluguel : repositorioAlugueis.listar()) {
                if (aluguel.getJogo() != null && guardado.getId().equals(aluguel.getJogo().getId())
                        && aluguel.getPlataforma() != null && antiga.equals(aluguel.getPlataforma())
                        && aluguel.getStatus() != StatusAluguel.DEVOLVIDO) {
                    throw new IllegalStateException("Não é possível tirar a plataforma \"" + antiga.getNome()
                        + "\" de \"" + guardado.getTitulo() + "\": há locação em andamento nela.");
                }
            }
        }
    }

    private static boolean contem(List<Plataforma> lista, Plataforma procurada) {
        for (Plataforma item : lista) {
            if (item != null && item.equals(procurada)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove o jogo. Não deixa remover se existe aluguel dele ainda em andamento.
     * Devolve false se o id não existe.
     */
    public synchronized boolean remover(Long id) {
        Sessao.exigirAdministrador();
        if (id == null) {
            throw new IllegalArgumentException("O id do jogo é obrigatório.");
        }
        Optional<Jogo> encontrado = repositorioJogos.buscarPorId(id);
        if (!encontrado.isPresent()) {
            return false;
        }
        for (Aluguel aluguel : repositorioAlugueis.listar()) {
            if (aluguel.getJogo() != null && id.equals(aluguel.getJogo().getId())
                    && aluguel.getStatus() != StatusAluguel.DEVOLVIDO) {
                throw new IllegalStateException("Não é possível remover o jogo \"" + encontrado.get().getTitulo()
                        + "\": existe aluguel em andamento.");
            }
        }
        return repositorioJogos.remover(id);
    }

    /** Jogos cujo título contém o texto (sem diferenciar maiúsculas, minúsculas e acentos). */
    public List<Jogo> buscarPorTitulo(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o texto do título a buscar.");
        }
        String procurado = normalizar(texto);
        List<Jogo> resultado = new ArrayList<>();
        for (Jogo jogo : repositorioJogos.listar()) {
            if (jogo.getTitulo() != null && normalizar(jogo.getTitulo()).contains(procurado)) {
                resultado.add(jogo);
            }
        }
        return resultado;
    }

    /** Jogos do gênero informado (sem diferenciar maiúsculas, minúsculas e acentos). */
    public List<Jogo> listarPorGenero(String genero) {
        if (genero == null || genero.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o gênero a buscar.");
        }
        String procurado = normalizar(genero);
        List<Jogo> resultado = new ArrayList<>();
        for (Jogo jogo : repositorioJogos.listar()) {
            if (jogo.getGenero() != null && normalizar(jogo.getGenero()).equals(procurado)) {
                resultado.add(jogo);
            }
        }
        return resultado;
    }

    /** Jogos disponíveis para a plataforma de id informado. */
    public List<Jogo> listarPorPlataforma(Long plataformaId) {
        if (plataformaId == null) {
            throw new IllegalArgumentException("O id da plataforma é obrigatório.");
        }
        List<Jogo> resultado = new ArrayList<>();
        for (Jogo jogo : repositorioJogos.listar()) {
            if (jogo.getPlataformas() == null) {
                continue;
            }
            for (Plataforma plataforma : jogo.getPlataformas()) {
                if (plataforma != null && plataformaId.equals(plataforma.getId())) {
                    resultado.add(jogo);
                    break;
                }
            }
        }
        return resultado;
    }

    private static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase();
    }

    public Optional<Jogo> buscarPorId(Long id) {
        if (id == null) {
           return Optional.empty();
        }

        return repositorioJogos.buscarPorId(id);
    }

    public List<Jogo> listar (){
        return repositorioJogos.listar();
    }
}
