package negocio;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Cliente da API pública FreeToGame. */
public class FreeToGameService {
    private static final URI ENDPOINT = URI.create("https://www.freetogame.com/api/games");
    private static final Pattern OBJETO = Pattern.compile("\\{[^{}]*}");
    private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public DadosJogo buscarPorTitulo(String titulo) {
        String procurado = validarTitulo(titulo).toLowerCase(Locale.ROOT);
        DadosJogo parcial = null;
        for (DadosJogo jogo : listarJogos()) {
            String nome = jogo.getTitulo().toLowerCase(Locale.ROOT);
            if (nome.equals(procurado)) return jogo;
            if (parcial == null && nome.contains(procurado)) parcial = jogo;
        }
        if (parcial != null) return parcial;
        throw new IllegalArgumentException("Nenhum jogo com esse título foi encontrado na FreeToGame.");
    }

    public List<DadosJogo> listarJogos() {
        try {
            HttpRequest requisicao = HttpRequest.newBuilder(ENDPOINT).timeout(Duration.ofSeconds(30)).GET().build();
            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() != 200) throw new IllegalStateException("A FreeToGame não respondeu à consulta agora.");
            List<DadosJogo> jogos = new ArrayList<>();
            Matcher objetos = OBJETO.matcher(resposta.body());
            while (objetos.find()) {
                String objeto = objetos.group();
                String titulo = texto(objeto, "title");
                if (titulo != null) jogos.add(new DadosJogo(titulo, texto(objeto, "genre"), texto(objeto, "platform"),
                        texto(objeto, "developer"), texto(objeto, "publisher"), texto(objeto, "game_url"), texto(objeto, "thumbnail")));
            }
            return jogos;
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível acessar a FreeToGame. Verifique sua conexão.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("A consulta à FreeToGame foi interrompida.", e);
        }
    }

    private static String validarTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) throw new IllegalArgumentException("Informe o título do jogo para buscar os dados.");
        return titulo.trim();
    }

    private static String texto(String objeto, String chave) {
        Matcher campo = Pattern.compile("\\\"" + Pattern.quote(chave) + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"").matcher(objeto);
        return campo.find() ? campo.group(1).replace("\\\"", "\"").replace("\\/", "/").replace("\\\\", "\\") : null;
    }

    public static final class DadosJogo {
        private final String titulo, genero, plataformas, desenvolvedora, publicadora, urlReferencia, urlThumbnail;
        private DadosJogo(String titulo, String genero, String plataformas, String desenvolvedora, String publicadora, String urlReferencia, String urlThumbnail) {
            this.titulo = titulo; this.genero = genero; this.plataformas = plataformas; this.desenvolvedora = desenvolvedora;
            this.publicadora = publicadora; this.urlReferencia = urlReferencia; this.urlThumbnail = urlThumbnail;
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
