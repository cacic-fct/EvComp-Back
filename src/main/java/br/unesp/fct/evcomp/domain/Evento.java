package br.unesp.fct.evcomp.domain;

import jakarta.persistence.*;
import java.util.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evento")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEvento")
    private Integer id;

    @Column(nullable = false, unique = true)
    private String titulo;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_termino", nullable = false)
    private LocalDate dataFim;

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    private String link;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contabilizacao", nullable = false)
    private TipoContabilizacao tipoContabilizacao;

    @Transient
    private Atividade[] atividade;

    @Transient
    private Participante[] participante;

    @Transient
    private Relatorio[] relatorio;

    public Evento() {
    }

    public Evento(String titulo, LocalDate dataInicio, LocalDate dataFim, String descricao, String link, TipoContabilizacao tipoContabilizacao) {
        this.titulo = titulo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.descricao = descricao;
        this.link = link;
        this.tipoContabilizacao = tipoContabilizacao;
    }

    public java.util.Map<String, Object> pegarDadosEvento() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
    
        map.put("id", this.id);
        map.put("titulo", this.titulo);
        map.put("dataInicio", this.dataInicio);
        map.put("dataFim", this.dataFim);
        map.put("descricao", this.descricao);
        map.put("link", this.link);
        map.put("tipoContabilizacao", this.tipoContabilizacao);
    
        return map;
    }

    public Evento criarEvento(String titulo, LocalDate dataInicio, LocalDate dataTermino, String descricao, String link) {
        return new Evento(titulo, dataInicio, dataTermino, descricao, link, null);
    }

    public void removerAtividade(Atividade atividade) {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public TipoContabilizacao getTipoContabilizacao() {
        return tipoContabilizacao;
    }

    public void setTipoContabilizacao(TipoContabilizacao tipoContabilizacao) {
        this.tipoContabilizacao = tipoContabilizacao;
    }

}
