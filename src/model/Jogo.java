package model;

import java.util.ArrayList;
import java.util.List;

import ENUMS.ClassificacaoIndicativa;

public class Jogo {
  private Long id;
  private String titulo;
  private ClassificacaoIndicativa classificacaoIndicativa;
  private String desenvolvedora;
  private String genero;
  private String publicadora;
  private Double preco;
  private List<Plataforma> plataformas = new ArrayList<>();
  private int quantidadeDisponivelPorPlataforma;

  public Jogo(Long id, String titulo, ClassificacaoIndicativa classificacaoIndicativa, String desenvolvedora,
      String genero, String publicadora, Double preco, List<Plataforma> plataformas,
      int quantidadeDisponivelPorPlataforma) {
    this.id = id;
    this.titulo = titulo;
    this.classificacaoIndicativa = classificacaoIndicativa;
    this.desenvolvedora = desenvolvedora;
    this.genero = genero;
    this.publicadora = publicadora;
    this.preco = preco;
    this.plataformas = plataformas;
    this.quantidadeDisponivelPorPlataforma = quantidadeDisponivelPorPlataforma;
  }

  public Long getId() {
    return id;
  }

  public String getTitulo() {
    return titulo;
  }

  public ClassificacaoIndicativa getClassificacaoIndicativa() {
    return classificacaoIndicativa;
  }

  public String getDesenvolvedora() {
    return desenvolvedora;
  }

  public String getGenero() {
    return genero;
  }

  public String getPublicadora() {
    return publicadora;
  }

  public Double getPreco() {
    return preco;
  }

  public List<Plataforma> getPlataformas() {
    return plataformas;
  }

  public int getQuantidadeDisponivelPorPlataforma() {
    return quantidadeDisponivelPorPlataforma;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public void setClassificacaoIndicativa(ClassificacaoIndicativa classificacaoIndicativa) {
    this.classificacaoIndicativa = classificacaoIndicativa;
  }

  public void setDesenvolvedora(String desenvolvedora) {
    this.desenvolvedora = desenvolvedora;
  }

  public void setGenero(String genero) {
    this.genero = genero;
  }

  public void setPublicadora(String publicadora) {
    this.publicadora = publicadora;
  }

  public void setPreco(Double preco) {
    this.preco = preco;
  }

  public void setPlataformas(List<Plataforma> plataformas) {
    this.plataformas = plataformas;
  }

  public void setQuantidadeDisponivelPorPlataforma(int quantidadeDisponivelPorPlataforma) {
    this.quantidadeDisponivelPorPlataforma = quantidadeDisponivelPorPlataforma;
  }

  
}
