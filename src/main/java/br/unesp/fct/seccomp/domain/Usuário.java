package br.unesp.fct.seccomp.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuário {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false)
    protected String nome;

    @Column(nullable = false, unique = true)
    protected String email;

    @Column(nullable = false)
    protected String senha;

    @Column(name = "token_redefinicao")
    protected String tokenRedefinicao;

    public Usuário() {
    }

    public Usuário(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public boolean validarSenha(String senha, Usuário usuarioExiste) {
        // Implementação mock/stub do Astah
        return false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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
}
