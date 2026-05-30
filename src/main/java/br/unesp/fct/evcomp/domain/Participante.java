package br.unesp.fct.evcomp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "participantes")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Participante extends Usuário {

    @Column(name = "ra")
    private String RA;

    @Transient
    private Inscrição[] inscrição;

    @Transient
    private Certificado[] certificado;

    @Column(name = "eh_coletor", nullable = false)
    private boolean ehColetor = false;

    public Participante() {
        super();
    }

    public Participante(String nome, String email, String senha) {
        super(nome, email, senha);
        this.ehColetor = false;
    }

    public Participante(String nome, String email, String senha, String ra) {
        super(nome, email, senha);
        this.RA = ra;
        this.ehColetor = false;
    }

    public Participante criarParticipante(String nome, String email, String senha) {
        return new Participante(nome, email, senha);
    }

    public String getRA() {
        return RA;
    }

    public void setRA(String RA) {
        this.RA = RA;
    }

    public boolean isEhColetor() {
        return ehColetor;
    }

    public void setEhColetor(boolean ehColetor) {
        this.ehColetor = ehColetor;
    }
}
