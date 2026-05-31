package br.unesp.fct.evcomp.domain;

import java.time.LocalDateTime;

public class Sessao {

    private Integer id;

    private String token;

    private LocalDateTime dataInicio;

    private LocalDateTime dataExpiracao;

    private boolean ativa;

    private Usuário usuario;

    public Sessao() {
    }

    public Sessao(String token, LocalDateTime dataInicio, LocalDateTime dataExpiracao, boolean ativa, Usuário usuario) {
        this.token = token;
        this.dataInicio = dataInicio;
        this.dataExpiracao = dataExpiracao;
        this.ativa = ativa;
        this.usuario = usuario;
    }

    public boolean iniciarSessao(Usuário usuarioExiste) {
        if (usuarioExiste != null) {
            this.token = java.util.UUID.randomUUID().toString();
            this.dataInicio = LocalDateTime.now();
            this.dataExpiracao = LocalDateTime.now().plusHours(2);
            this.ativa = true;
            this.usuario = usuarioExiste;
            return true;
        }
        return false;
    }

    public void invalidarSessao() {
        this.ativa = false;
        this.dataExpiracao = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public void setDataExpiracao(LocalDateTime dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public Usuário getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuário usuario) {
        this.usuario = usuario;
    }
}
