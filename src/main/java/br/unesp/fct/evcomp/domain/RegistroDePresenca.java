package br.unesp.fct.evcomp.domain;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "presença", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"idUsuário", "idAtividade"})
})
public class RegistroDePresenca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPresença")
    private Integer id;

    @Column(name = "data_registro", nullable = false)
    private Date dataRegistro;

    @Column(nullable = false)
    private boolean presente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuário", nullable = false)
    private Participante participante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAtividade", nullable = false)
    private Atividade atividade;

    public RegistroDePresenca() {
    }

    public RegistroDePresenca(Date dataRegistro, boolean presente, Participante participante, Atividade atividade) {
        this.dataRegistro = dataRegistro;
        this.presente = presente;
        this.participante = participante;
        this.atividade = atividade;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(Date dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public boolean isPresente() {
        return presente;
    }

    public void setPresente(boolean presente) {
        this.presente = presente;
    }

    public Participante getParticipante() {
        return participante;
    }

    public void setParticipante(Participante participante) {
        this.participante = participante;
    }

    public Atividade getAtividade() {
        return atividade;
    }

    public void setAtividade(Atividade atividade) {
        this.atividade = atividade;
    }
}
