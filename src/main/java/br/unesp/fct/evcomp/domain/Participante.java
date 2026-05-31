package br.unesp.fct.evcomp.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Column;

@Entity
@DiscriminatorValue("PAR")
public class Participante extends Usuário {

    @Column(name = "ra", columnDefinition = "CHAR(9)")
    private String RA;

    @Transient
    private Inscrição[] inscrição;

    @Transient
    private Certificado[] certificado;


    public Participante() {
        super();
    }

    public Participante(String nome, String sobrenome, String email, String senha) {
        super(nome, sobrenome, email, senha);
    }

    public Participante(String nome, String sobrenome, String email, String senha, String ra) {
        super(nome, sobrenome, email, senha);
        this.RA = ra;
    }

    public Participante criarParticipante(String nome, String sobrenome, String email, String senha) {
        return new Participante(nome, sobrenome, email, senha);
    }

    public String getRA() {
        return RA;
    }

    public void setRA(String RA) {
        this.RA = RA;
    }
}
