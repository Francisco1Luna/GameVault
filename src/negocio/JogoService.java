package negocio;

import dados.IRepositorioAlugueis;
import dados.IRepositorioJogos;
import model.Aluguel;
import model.Jogo;
import model.Plataforma;
import model.enums.StatusAluguel;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JogoService {

    private final IRepositorioJogos repositorioJogos;
    private final IRepositorioAlugueis repositorioAlugueis;

    public JogoService(IRepositorioJogos repositorioJogos, IRepositorioAlugueis repositorioAlugueis) {
        if (repositorioJogos == null) {
            throw new IllegalArgumentException("O repositorio de jogo não pode ser nulo.");
        }
        if (repositorioAlugueis == null) {
            throw new IllegalArgumentException("O repositório de aluguéis é obrigatório para verificar o uso do jogo.");
        }
        this.repositorioJogos = repositorioJogos;
        this.repositorioAlugueis = repositorioAlugueis;
    }

    public Jogo salvar(Jogo jogo) {
        if (jogo == null) {
            throw new IllegalArgumentException("O jogo não pode ser vazio e deve possuir id.");
        }
        if (jogo.getTitulo() == null || jogo.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O jogo não pode ser vazaio e deve possuir um título.");
        }
       return repositorioJogos.salvar(jogo);

    }

    public void atualizar(Jogo jogo) {
         if (jogo == null || jogo.getId() == null) {
            throw new IllegalArgumentException("O jogo não pode ser vazio e deve possuir id.");
        }
        if (jogo == null || jogo.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O jogo não pode ser vazaio e deve possuir um título.");
        }
        repositorioJogos.atualizar(jogo);
    }

    /**
     * Remove o jogo. Não deixa remover se existe aluguel dele ainda em andamento.
     * Devolve false se o id não existe.
     */
    public synchronized boolean remover(Long id) {
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
