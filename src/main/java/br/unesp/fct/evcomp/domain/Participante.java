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

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "secret_seed", length = 32)
    private String secretSeed;

    @Transient
    private Inscrição[] inscrição;

    @Transient
    private Certificado[] certificado;


    public Participante() {
        super();
    }

    public Participante(String nomeCompleto, String email, String senha) {
        super(nomeCompleto, email, senha);
        this.secretSeed = gerarSecretSeed();
    }



    public static Participante criarParticipante(String nomeCompleto, String email, String senha) {
        return new Participante(nomeCompleto, email, senha);
    }

    public String getRA() {
        return RA;
    }

    public void setRA(String RA) {
        this.RA = RA;
    }

    public String getSecretSeed() {
        if (this.secretSeed == null) {
            this.secretSeed = gerarSecretSeed();
        }
        return secretSeed;
    }

    public void setSecretSeed(String secretSeed) {
        this.secretSeed = secretSeed;
    }

    private String gerarSecretSeed() {
        byte[] bytes = new byte[20];
        new java.security.SecureRandom().nextBytes(bytes);
        return new org.apache.commons.codec.binary.Base32().encodeToString(bytes).replace("=", "");
    }
}
