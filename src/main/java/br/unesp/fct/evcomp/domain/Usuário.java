package br.unesp.fct.evcomp.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "usuário")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario", discriminatorType = DiscriminatorType.STRING, columnDefinition = "CHAR(3)", length = 3)
public abstract class Usuário {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuário")
    protected Integer id;

    @Column(name = "nome_completo", nullable = false)
    protected String nomeCompleto;

    @Column(nullable = false, unique = true)
    protected String email;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "senha_hash")
    protected String senha;

    @Transient
    protected String tokenRedefinicao;

    public Usuário() {
    }

    public Usuário(String nomeCompleto, String email, String senha) {
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.senha = senha;
    }

    public boolean validarSenha(String senhaParaVerificar, Usuário usuarioExiste) {
        String hashNoBanco = usuarioExiste.getSenha();

        try {
            //TO-DO: Retirar o $2a$ e o else. Isso é só para TESTES!
            if (hashNoBanco != null && hashNoBanco.startsWith("$2a$")) {
                return org.mindrot.jbcrypt.BCrypt.checkpw(senhaParaVerificar, hashNoBanco);
            } else {
                return senhaParaVerificar.equals(hashNoBanco);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTokenRedefinicao() {
        return tokenRedefinicao;
    }

    public void setTokenRedefinicao(String tokenRedefinicao) {
        this.tokenRedefinicao = tokenRedefinicao;
    }

    @Transient
    public String getRole() {
        if (this instanceof br.unesp.fct.evcomp.domain.ColetorDePresenca) {
            return "COLETOR";
        } else if (this instanceof br.unesp.fct.evcomp.domain.Administrador) {
            return "ADMIN";
        }
        return "PARTICIPANTE";
    }
}
