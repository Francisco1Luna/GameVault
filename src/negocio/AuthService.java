package negocio;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import model.Cliente;
import model.ClienteAssinante;
import model.ClientePadrao;

/**
 * Cadastro, login e sessão. O "usuário atual" é o Cliente que fez login; é com base nele que
 * as vendas e locações são feitas (desconto de assinante, limite de locações etc.).
 *
 * O login é por CPF + senha. A senha nunca é guardada: o Cliente recebe só o hash (PBKDF2 com sal).
 * Como é um aplicativo de desktop de um usuário por vez, existe uma única sessão em memória.
 */
public class AuthService {

  /** Desconto (em %) dado ao Assinante nas vendas, definido no cadastro do plano. */
  public static final double PERCENTUAL_DESCONTO_ASSINANTE = 10.0;
  public static final int TAMANHO_MINIMO_SENHA = 6;

  private static final int ITERACOES = 65_000;
  private static final int TAMANHO_HASH_BITS = 256;
  private static final int TAMANHO_SAL_BYTES = 16;

  // Singleton: uma única instância em memória; use AuthService.getInstance().
  private static final AuthService INSTANCIA = new AuthService(ClienteService.getInstance());

  public static AuthService getInstance() {
    return INSTANCIA;
  }

  private final ClienteService clienteService;
  private final SecureRandom aleatorio = new SecureRandom();
  private Long idUsuarioAtual; // protegido por synchronized

  private AuthService(ClienteService clienteService) {
    if (clienteService == null) {
      throw new IllegalArgumentException("O serviço de clientes é obrigatório.");
    }
    this.clienteService = clienteService;
  }

  /**
   * Cadastra um cliente com senha. Assinante recebe o desconto padrão e a data de adesão de hoje.
   * Não faz login automaticamente.
   */
  public synchronized Cliente cadastrar(String nome, String cpf, LocalDate dataNascimento, String email,
      String telefone, String senha, boolean assinante) {
    if (nome == null || nome.trim().isEmpty()) {
      throw new IllegalArgumentException("O nome é obrigatório.");
    }
    String cpfNormalizado = normalizarCpf(cpf);
    if (cpfNormalizado.length() != 11) {
      throw new IllegalArgumentException("O CPF deve ter 11 dígitos.");
    }
    if (dataNascimento == null) {
      throw new IllegalArgumentException("A data de nascimento é obrigatória.");
    }
    if (dataNascimento.isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("A data de nascimento não pode ser futura.");
    }
    if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
      throw new IllegalArgumentException("A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
    }
    if (clienteService.buscarPorCpf(cpfNormalizado).isPresent()) {
      throw new IllegalArgumentException("Já existe um cliente cadastrado com este CPF.");
    }

    String emailLimpo = vazioParaNulo(email);
    String telefoneLimpo = vazioParaNulo(telefone);
    Cliente cliente;
    if (assinante) {
      cliente = new ClienteAssinante(null, nome.trim(), cpfNormalizado, dataNascimento, emailLimpo,
          telefoneLimpo, PERCENTUAL_DESCONTO_ASSINANTE, LocalDate.now());
    } else {
      cliente = new ClientePadrao(null, nome.trim(), cpfNormalizado, dataNascimento, emailLimpo, telefoneLimpo);
    }
    cliente.setSenhaHash(gerarHash(senha));
    return clienteService.salvar(cliente);
  }

  /** Faz login por CPF e senha. A mensagem de erro é a mesma para CPF desconhecido e senha errada. */
  public synchronized Cliente login(String cpf, String senha) {
    if (cpf == null || cpf.trim().isEmpty() || senha == null || senha.isEmpty()) {
      throw new IllegalArgumentException("Informe CPF e senha.");
    }
    Optional<Cliente> encontrado = clienteService.buscarPorCpf(normalizarCpf(cpf));
    if (!encontrado.isPresent() || !confere(senha, encontrado.get().getSenhaHash())) {
      throw new IllegalArgumentException("CPF ou senha inválidos.");
    }
    idUsuarioAtual = encontrado.get().getId();
    return encontrado.get();
  }

  public synchronized void logout() {
    idUsuarioAtual = null;
  }

  /** O cliente logado, ou vazio se ninguém fez login (ou se ele foi removido depois do login). */
  public synchronized Optional<Cliente> getUsuarioAtual() {
    if (idUsuarioAtual == null) {
      return Optional.empty();
    }
    Optional<Cliente> atual = clienteService.buscarPorId(idUsuarioAtual);
    if (!atual.isPresent()) {
      idUsuarioAtual = null;
    }
    return atual;
  }

  public boolean estaLogado() {
    return getUsuarioAtual().isPresent();
  }

  /** Usado pelas operações que exigem login (vendas, locações). */
  public Cliente exigirUsuarioAtual() {
    return getUsuarioAtual().orElseThrow(
        () -> new IllegalStateException("Nenhum usuário logado. Faça login para continuar."));
  }

  public boolean usuarioAtualEhAssinante() {
    return getUsuarioAtual().map(c -> c instanceof ClienteAssinante).orElse(false);
  }

  // ---- senha ----

  private String gerarHash(String senha) {
    byte[] sal = new byte[TAMANHO_SAL_BYTES];
    aleatorio.nextBytes(sal);
    return Base64.getEncoder().encodeToString(sal) + ":" + Base64.getEncoder().encodeToString(derivar(senha, sal));
  }

  private boolean confere(String senha, String armazenado) {
    if (armazenado == null) {
      return false; // cliente cadastrado sem credenciais (ex.: direto pelo ClienteService)
    }
    String[] partes = armazenado.split(":");
    if (partes.length != 2) {
      return false;
    }
    try {
      byte[] sal = Base64.getDecoder().decode(partes[0]);
      byte[] esperado = Base64.getDecoder().decode(partes[1]);
      return MessageDigest.isEqual(esperado, derivar(senha, sal));
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private static byte[] derivar(String senha, byte[] sal) {
    try {
      PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), sal, ITERACOES, TAMANHO_HASH_BITS);
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Não foi possível processar a senha.", e);
    }
  }

  private static String normalizarCpf(String cpf) {
    return cpf == null ? "" : cpf.replaceAll("\\D", "");
  }

  private static String vazioParaNulo(String texto) {
    return texto == null || texto.trim().isEmpty() ? null : texto.trim();
  }
}
