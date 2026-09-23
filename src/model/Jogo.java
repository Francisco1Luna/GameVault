package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ENUMS.ClassificacaoIndicativa;

public class Jogo {

  private Long id;
  private String titulo;
  private ClassificacaoIndicativa classificacaoIndicativa;
  private String desenvolvedora;
  private String genero;
  private String publicadora;
  private Double preco;
  private List<Plataforma> plataformas;
  private Map<Plataforma, Integer> estoquePorPlataforma = new HashMap<>();
  private String urlThumbnail;
  private String urlReferencia;
  private boolean dadoDesatualizado;

  public Jogo(Long id, String titulo, ClassificacaoIndicativa classificacaoIndicativa, String desenvolvedora,
      String genero, String publicadora, Double preco, List<Plataforma> plataformas,
      Map<Plataforma, Integer> estoquePorPlataforma, String urlThumbnail, String urlReferencia) {
    this.id = id;
    this.titulo = titulo;
    this.classificacaoIndicativa = classificacaoIndicativa;
    this.desenvolvedora = desenvolvedora;
    this.genero = genero;
    this.publicadora = publicadora;
    this.preco = preco;
    this.plataformas = plataformas;
    this.estoquePorPlataforma = estoquePorPlataforma;
    this.urlThumbnail = urlThumbnail;
    this.urlReferencia = urlReferencia;
    this.dadoDesatualizado = false;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public ClassificacaoIndicativa getClassificacaoIndicativa() {
    return classificacaoIndicativa;
  }

  public void setClassificacaoIndicativa(ClassificacaoIndicativa classificacaoIndicativa) {
    this.classificacaoIndicativa = classificacaoIndicativa;
  }

  public String getDesenvolvedora() {
    return desenvolvedora;
  }

  public void setDesenvolvedora(String desenvolvedora) {
    this.desenvolvedora = desenvolvedora;
  }

  public String getGenero() {
    return genero;
  }

  public void setGenero(String genero) {
    this.genero = genero;
  }

  public String getPublicadora() {
    return publicadora;
  }

  public void setPublicadora(String publicadora) {
    this.publicadora = publicadora;
  }

  public Double getPreco() {
    return preco;
  }

  public void setPreco(Double preco) {
    this.preco = preco;
  }

  public List<Plataforma> getPlataformas() {
    return plataformas;
  }

  public void setPlataformas(List<Plataforma> plataformas) {
    this.plataformas = plataformas;
  }

  public Map<Plataforma, Integer> getEstoquePorPlataforma() {
    return estoquePorPlataforma;
  }

  public void setEstoquePorPlataforma(Map<Plataforma, Integer> estoquePorPlataforma) {
    this.estoquePorPlataforma = estoquePorPlataforma;
  }

  public String getUrlThumbnail() {
    return urlThumbnail;
  }

  public void setUrlThumbnail(String urlThumbnail) {
    this.urlThumbnail = urlThumbnail;
  }

  public String getUrlReferencia() {
    return urlReferencia;
  }

  public void setUrlReferencia(String urlReferencia) {
    this.urlReferencia = urlReferencia;
  }

  public boolean isDadoDesatualizado() {
    return dadoDesatualizado;
  }

  public void setDadoDesatualizado(boolean dadoDesatualizado) {
    this.dadoDesatualizado = dadoDesatualizado;
  }
}
