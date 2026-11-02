package gui;

import dados.*;
import negocio.*;

public class Contexto {

    private static final Contexto INSTANCIA = new Contexto();

    private final JogoService jogoService;
    private final ClienteService clienteService;
    private final EstoqueService estoqueService;
    private final AluguelService aluguelService;
    private final PlataformaService plataformaService;

    private Contexto() {
        // Repositorios
        IRepositorioJogos repoJogos = new RepositorioJogosLista();
        IRepositorioClientes repoClientes = new RepositorioClientesLista();
        IRepositorioAlugueis repoAlugueis = new RepositorioAlugueisLista();
        IRepositorioPlataformas repoPlataformas = new RepositoriosPlataformas();

        // Services
        this.jogoService = new JogoService(repoJogos, repoAlugueis);
        this.clienteService = new ClienteService(repoClientes);
        this.estoqueService = new EstoqueService(repoJogos);
        this.aluguelService = new AluguelService(estoqueService, repoAlugueis);
        this.plataformaService = new PlataformaService(repoPlataformas, repoJogos);
    }

    public static Contexto get() { return INSTANCIA; }

    public JogoService getJogoService() { return jogoService; }
    public ClienteService getClienteService() { return clienteService; }
    public EstoqueService getEstoqueService() { return estoqueService; }
    public AluguelService getAluguelService() { return aluguelService; }
    public PlataformaService getPlataformaService() { return plataformaService; }
}