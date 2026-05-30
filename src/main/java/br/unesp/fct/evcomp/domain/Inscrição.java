package br.unesp.fct.evcomp.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inscricoes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"participante_id", "evento_id"})
})
public class Inscrição {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_inscricao", nullable = false)
    private Date dataInscricao;

    @Column(nullable = false)
    private boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participante_id", nullable = false)
    private Participante participante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "inscricao_atividades",
        joinColumns = @JoinColumn(name = "inscricao_id"),
        inverseJoinColumns = @JoinColumn(name = "atividade_id")
    )
    private Atividade[] atividade;

    public Inscrição() {
    }

    public Inscrição(Date dataInscricao, boolean status, Participante participante, Evento evento, Atividade[] atividade) {
        this.dataInscricao = dataInscricao;
        this.status = status;
        this.participante = participante;
        this.evento = evento;
        this.atividade = atividade;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Atividade[] getAtividade() {
        return atividade;
    }

    public void setAtividade(Atividade[] atividade) {
        this.atividade = atividade;
    }
}
