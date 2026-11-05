package negocio;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Consulta o catálogo público FreeToGame para enriquecer o cadastro de jogos. */
public class FreeToGameService {
    private static final URI ENDPOINT = URI.create("https://www.freetogame.com/api/games");
    private static final Pattern OBJETO = Pattern.compile("\\{[^{}]*}");
    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    public DadosJogo buscarPorTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o título do jogo para buscar os dados.");
        }
        try {
            HttpRequest requisicao = HttpRequest.newBuilder(ENDPOINT)
                    .timeout(Duration.ofSeconds(20)).GET().build();
            HttpResponse<String> resposta = CLIENTE.send(requisicao, HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() != 200) {
                throw new IllegalStateException("A FreeToGame não respondeu à consulta agora.");
            }
            return localizar(resposta.body(), titulo);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível acessar a FreeToGame. Verifique sua conexão.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("A consulta à FreeToGame foi interrompida.", e);
        }
    }

    /** Retorna todos os jogos publicados no catálogo atual da FreeToGame. */
    public List<DadosJogo> listarJogos() {
        try {
            HttpRequest requisicao = HttpRequest.newBuilder(ENDPOINT)
                    .timeout(Duration.ofSeconds(30)).GET().build();
            HttpResponse<String> resposta = CLIENTE.send(requisicao, HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() != 200) {
                throw new IllegalStateException("A FreeToGame não respondeu à consulta agora.");
            }
            List<DadosJogo> jogos = new ArrayList<>();
            Matcher objetos = OBJETO.matcher(resposta.body());
            while (objetos.find()) {
                String objeto = objetos.group();
                String titulo = texto(objeto, "title");
                if (titulo != null) {
                    jogos.add(new DadosJogo(titulo, texto(objeto, "genre"), texto(objeto, "platform"),
                            texto(objeto, "developer"), texto(objeto, "publisher"), texto(objeto, "game_url"),
                            texto(objeto, "thumbnail")));
                }
            }
            return jogos;
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível acessar a FreeToGame. Verifique sua conexão.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("A importação da FreeToGame foi interrompida.", e);
        }
    }

    private DadosJogo localizar(String json, String titulo) {
        String procurado = titulo.trim().toLowerCase(Locale.ROOT);
        DadosJogo aproximado = null;
        Matcher objetos = OBJETO.matcher(json);
        while (objetos.find()) {
            String objeto = objetos.group();
            String nome = texto(objeto, "title");
            if (nome == null) continue;
            DadosJogo dado = new DadosJogo(nome, texto(objeto, "genre"), texto(objeto, "platform"),
                    texto(objeto, "developer"), texto(objeto, "publisher"), texto(objeto, "game_url"),
                    texto(objeto, "thumbnail"));
            if (nome.toLowerCase(Locale.ROOT).equals(procurado)) return dado;
            if (aproximado == null && nome.toLowerCase(Locale.ROOT).contains(procurado)) aproximado = dado;
        }
        if (aproximado != null) return aproximado;
        throw new IllegalArgumentException("Nenhum jogo com esse título foi encontrado na FreeToGame.");
    }

    private static String texto(String objeto, String chave) {
        Pattern campo = Pattern.compile("\\\"" + Pattern.quote(chave) + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"");
        Matcher resultado = campo.matcher(objeto);
        return resultado.find() ? desescapar(resultado.group(1)) : null;
    }

    private static String desescapar(String valor) {
        return valor.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\/", "/");
    }

    public static final class DadosJogo {
        private final String titulo, genero, plataformas, desenvolvedora, publicadora, urlReferencia, urlThumbnail;

        private DadosJogo(String titulo, String genero, String plataformas, String desenvolvedora,
                          String publicadora, String urlReferencia, String urlThumbnail) {
            this.titulo = titulo;
            this.genero = genero;
            this.plataformas = plataformas;
            this.desenvolvedora = desenvolvedora;
            this.publicadora = publicadora;
            this.urlReferencia = urlReferencia;
            this.urlThumbnail = urlThumbnail;
        }

        public String getTitulo() { return titulo; }
        public String getGenero() { return genero; }
        public String getPlataformas() { return plataformas; }
        public String getDesenvolvedora() { return desenvolvedora; }
        public String getPublicadora() { return publicadora; }
        public String getUrlReferencia() { return urlReferencia; }
        public String getUrlThumbnail() { return urlThumbnail; }
    }
}
