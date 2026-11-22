package negocio;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Cliente da API pública FreeToGame. A lista completa é baixada uma vez e guardada em memória por
 * alguns minutos (cache compartilhado), então buscar vários títulos seguidos não baixa tudo de novo.
 */
public class FreeToGameService {
    private static final URI ENDPOINT = URI.create("https://www.freetogame.com/api/games");
    private static final Duration VALIDADE_CACHE = Duration.ofMinutes(10);
    private static final HttpClient CLIENTE = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    // cache compartilhado por todas as instâncias (protegido pelo bloqueio da classe)
    private static List<DadosJogo> cache;
    private static Instant cacheEm;

    // de onde vem o JSON; trocado só nos testes para não depender da internet
    static Supplier<String> fonteJson = FreeToGameService::baixarJson;

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

    /** Lista completa da API (do cache se ainda for recente). */
    public List<DadosJogo> listarJogos() {
        synchronized (FreeToGameService.class) {
            if (cache == null || cacheEm == null || Instant.now().isAfter(cacheEm.plus(VALIDADE_CACHE))) {
                cache = interpretar(fonteJson.get());
                cacheEm = Instant.now();
            }
            return new ArrayList<>(cache);
        }
    }

    /** Descarta o cache: a próxima consulta baixa a lista de novo. */
    public static void limparCache() {
        synchronized (FreeToGameService.class) {
            cache = null;
            cacheEm = null;
        }
    }

    private static String baixarJson() {
        try {
            HttpRequest requisicao = HttpRequest.newBuilder(ENDPOINT).timeout(Duration.ofSeconds(30)).GET().build();
            HttpResponse<String> resposta = CLIENTE.send(requisicao, HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() != 200) throw new IllegalStateException("A FreeToGame não respondeu à consulta agora.");
            return resposta.body();
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível acessar a FreeToGame. Verifique sua conexão.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("A consulta à FreeToGame foi interrompida.", e);
        }
    }

    /** Converte a resposta (um array JSON de objetos) em jogos; ignora os objetos sem título. */
    static List<DadosJogo> interpretar(String json) {
        Object raiz;
        try {
            raiz = new LeitorJson(json).lerDocumento();
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("A resposta da FreeToGame veio em um formato inesperado.", e);
        }
        if (!(raiz instanceof List)) {
            throw new IllegalStateException("A resposta da FreeToGame veio em um formato inesperado.");
        }
        List<DadosJogo> jogos = new ArrayList<>();
        for (Object item : (List<?>) raiz) {
            if (!(item instanceof Map)) continue;
            Map<?, ?> objeto = (Map<?, ?>) item;
            String titulo = texto(objeto, "title");
            if (titulo != null && !titulo.trim().isEmpty()) {
                jogos.add(new DadosJogo(titulo, texto(objeto, "genre"), texto(objeto, "platform"),
                        texto(objeto, "developer"), texto(objeto, "publisher"), texto(objeto, "game_url"),
                        texto(objeto, "thumbnail")));
            }
        }
        return jogos;
    }

    private static String texto(Map<?, ?> objeto, String chave) {
        Object valor = objeto.get(chave);
        return valor instanceof String ? (String) valor : null;
    }

    private static String validarTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) throw new IllegalArgumentException("Informe o título do jogo para buscar os dados.");
        return titulo.trim();
    }

    /**
     * Leitor de JSON mínimo (objetos, arrays, strings, números, true/false/null). Respeita chaves
     * e colchetes dentro de textos e decodifica todas as sequências de escape, inclusive \\uXXXX.
     * Lança IllegalArgumentException se o texto não for JSON válido.
     */
    private static final class LeitorJson {
        private final String texto;
        private int pos;

        LeitorJson(String texto) {
            if (texto == null) throw new IllegalArgumentException("JSON vazio.");
            this.texto = texto;
        }

        Object lerDocumento() {
            pularEspacos();
            Object valor = lerValor();
            pularEspacos();
            if (pos != texto.length()) throw erro("conteúdo depois do fim do JSON");
            return valor;
        }

        private Object lerValor() {
            if (pos >= texto.length()) throw erro("fim inesperado");
            char c = texto.charAt(pos);
            switch (c) {
                case '{': return lerObjeto();
                case '[': return lerArray();
                case '"': return lerString();
                case 't': esperar("true"); return Boolean.TRUE;
                case 'f': esperar("false"); return Boolean.FALSE;
                case 'n': esperar("null"); return null;
                default:
                    if (c == '-' || (c >= '0' && c <= '9')) return lerNumero();
                    throw erro("caractere inesperado '" + c + "'");
            }
        }

        private Map<String, Object> lerObjeto() {
            Map<String, Object> objeto = new LinkedHashMap<>();
            pos++; // {
            pularEspacos();
            if (consumir('}')) return objeto;
            while (true) {
                pularEspacos();
                if (pos >= texto.length() || texto.charAt(pos) != '"') throw erro("esperava o nome de um campo");
                String chave = lerString();
                pularEspacos();
                if (!consumir(':')) throw erro("esperava ':'");
                pularEspacos();
                objeto.put(chave, lerValor());
                pularEspacos();
                if (consumir(',')) continue;
                if (consumir('}')) return objeto;
                throw erro("esperava ',' ou '}'");
            }
        }

        private List<Object> lerArray() {
            List<Object> lista = new ArrayList<>();
            pos++; // [
            pularEspacos();
            if (consumir(']')) return lista;
            while (true) {
                pularEspacos();
                lista.add(lerValor());
                pularEspacos();
                if (consumir(',')) continue;
                if (consumir(']')) return lista;
                throw erro("esperava ',' ou ']'");
            }
        }

        private String lerString() {
            StringBuilder sb = new StringBuilder();
            pos++; // aspas de abertura
            while (pos < texto.length()) {
                char c = texto.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                if (pos >= texto.length()) break;
                char e = texto.charAt(pos++);
                switch (e) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (pos + 4 > texto.length()) throw erro("escape \\u incompleto");
                        try {
                            sb.append((char) Integer.parseInt(texto.substring(pos, pos + 4), 16));
                        } catch (NumberFormatException ex) {
                            throw erro("escape \\u inválido");
                        }
                        pos += 4;
                        break;
                    default: throw erro("escape inválido \\" + e);
                }
            }
            throw erro("texto sem aspas de fechamento");
        }

        private String lerNumero() {
            int inicio = pos;
            while (pos < texto.length() && "+-0123456789.eE".indexOf(texto.charAt(pos)) >= 0) pos++;
            return texto.substring(inicio, pos);
        }

        private void esperar(String palavra) {
            if (!texto.startsWith(palavra, pos)) throw erro("esperava " + palavra);
            pos += palavra.length();
        }

        private boolean consumir(char c) {
            if (pos < texto.length() && texto.charAt(pos) == c) {
                pos++;
                return true;
            }
            return false;
        }

        private void pularEspacos() {
            while (pos < texto.length() && Character.isWhitespace(texto.charAt(pos))) pos++;
        }

        private IllegalArgumentException erro(String motivo) {
            return new IllegalArgumentException("JSON inválido na posição " + pos + ": " + motivo);
        }
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
