
package br.unesp.fct.evcomp.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inscrição", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"idUsuário", "idEvento"})
})
public class Inscrição {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInscrição")
    private Integer id;

    @Column(name = "data_inscricao", nullable = false)
    private Date dataInscricao;

    @Column(nullable = false)
    private boolean status;

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuário", nullable = false)
    private Participante participante;

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEvento", nullable = false)
    private Evento evento;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "inscrição_atividade",
        joinColumns = @JoinColumn(name = "idInscrição"),
        inverseJoinColumns = @JoinColumn(name = "idAtividade")
    )
    private List<Atividade> atividade = new ArrayList<>();

    public Inscrição() {
    }

    public Inscrição(Date dataInscricao, boolean status, Participante participante, Evento evento, List<Atividade> atividade) {
        this.dataInscricao = dataInscricao;
        this.status = status;
        this.participante = participante;
        this.evento = evento;
        this.atividade = atividade;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDataInscricao() {
        return dataInscricao;
    }

    public void setDataInscricao(Date dataInscricao) {
        this.dataInscricao = dataInscricao;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Participante getParticipante() {
        return participante;
    }

    public void setParticipante(Participante participante) {
        this.participante = participante;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public List<Atividade> getAtividade() {
        return atividade;
    }

    public void setAtividade(List<Atividade> atividade) {
        this.atividade = atividade;
    }
}
