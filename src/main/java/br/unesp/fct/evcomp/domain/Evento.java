package br.unesp.fct.evcomp.domain;

import jakarta.persistence.*;
import java.util.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String titulo;

    @Column(name = "data_inicio", nullable = false)
    private Date dataInicio;

    @Column(name = "data_fim", nullable = false)
    private Date dataFim;

    @Column(nullable = false, length = 1000)
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

    public Evento(String titulo, Date dataInicio, Date dataFim, String descricao, String link, TipoContabilizacao tipoContabilizacao) {
        this.titulo = titulo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.descricao = descricao;
        this.link = link;
        this.tipoContabilizacao = tipoContabilizacao;
    }

    public Object pegarDadosEvento(Evento evento) {
        return null;
    }

    public Evento criarEvento(String titulo, Date dataInicio, Date dataTermino, String descricao, String link) {
        return new Evento(titulo, dataInicio, dataTermino, descricao, link, null);
    }

    public void removerAtividade(Atividade atividade) {
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

    public LocalDate getDataInicio() {
        return dataInicio == null ? null : dataInicio.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio == null ? null : Date.from(dataInicio.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
    }

    public LocalDate getDataFim() {
        return dataFim == null ? null : dataFim.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim == null ? null : Date.from(dataFim.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
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
