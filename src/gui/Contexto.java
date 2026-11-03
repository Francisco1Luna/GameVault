package gui;

import negocio.*;

/**
 * Ponto de acesso da interface aos services. Os services (e os repositórios por trás deles) são
 * Singletons: existe uma única instância de cada um em memória, então telas diferentes enxergam
 * os mesmos dados.
 */
public class Contexto {

    private static final Contexto INSTANCIA = new Contexto();

    private final JogoService jogoService;
    private final ClienteService clienteService;
    private final EstoqueService estoqueService;
    private final AluguelService aluguelService;
    private final DevolucaoService devolucaoService;
    private final VendaService vendaService;
    private final FinanceiroService financeiroService;
    private final PlataformaService plataformaService;

    private Contexto() {
        this.jogoService = JogoService.getInstance();
        this.clienteService = ClienteService.getInstance();
        this.estoqueService = EstoqueService.getInstance();
        this.aluguelService = AluguelService.getInstance();
        this.devolucaoService = DevolucaoService.getInstance();
        this.vendaService = VendaService.getInstance();
        this.financeiroService = FinanceiroService.getInstance();
        this.plataformaService = PlataformaService.getInstance();
    }

    public static Contexto get() { return INSTANCIA; }

    public JogoService getJogoService() { return jogoService; }
    public ClienteService getClienteService() { return clienteService; }
    public EstoqueService getEstoqueService() { return estoqueService; }
    public AluguelService getAluguelService() { return aluguelService; }
    public DevolucaoService getDevolucaoService() { return devolucaoService; }
    public VendaService getVendaService() { return vendaService; }
    public FinanceiroService getFinanceiroService() { return financeiroService; }
    public PlataformaService getPlataformaService() { return plataformaService; }
}
