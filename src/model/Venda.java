package model;

import java.time.LocalDate;
import java.util.List;

public class Venda {

  private Long id;
  private Cliente cliente;
  private LocalDate dataVenda;
  private List<ItemVenda> itens;
  private Double valorTotal;

  public Venda(Long id, Cliente cliente, LocalDate dataVenda, List<ItemVenda> itens, Double valorTotal) {
    this.id = id;
    this.cliente = cliente;
    this.dataVenda = dataVenda;
    this.itens = itens;
    this.valorTotal = valorTotal;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Cliente getCliente() {
    return cliente;
  }

  public void setCliente(Cliente cliente) {
    this.cliente = cliente;
  }

  public LocalDate getDataVenda() {
    return dataVenda;
  }

  public void setDataVenda(LocalDate dataVenda) {
    this.dataVenda = dataVenda;
  }

  public List<ItemVenda> getItens() {
    return itens;
  }

  public void setItens(List<ItemVenda> itens) {
    this.itens = itens;
  }

  public Double getValorTotal() {
    return valorTotal;
  }

  public void setValorTotal(Double valorTotal) {
    this.valorTotal = valorTotal;
  }
}
