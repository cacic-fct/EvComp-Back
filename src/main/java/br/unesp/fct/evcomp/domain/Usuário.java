package br.unesp.fct.evcomp.domain;

import jakarta.persistence.*;
import jakarta.persistence.DiscriminatorType;

@Entity
@Table(name = "usuário")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario", discriminatorType = DiscriminatorType.STRING, columnDefinition = "CHAR(3)", length = 3)
public abstract class Usuário {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuário")
    protected Integer id;

    @Column(nullable = false)
    protected String nome;

    @Column(name = "sobrenome", nullable = false)
    protected String sobrenome;

    @Column(nullable = false, unique = true)
    protected String email;

    @Column(name = "senha_hash")
    protected String senha;

    @Transient
    protected String tokenRedefinicao;

    public Usuário() {
    }

    public Usuário(String nome, String sobrenome, String email, String senha) {
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.email = email;
        this.senha = senha;
    }

    public boolean validarSenha(String senhaParaVerificar, Usuário usuarioExiste) {
        String hashNoBanco = usuarioExiste.getSenha();
        try {
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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
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
