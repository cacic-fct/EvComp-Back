package br.unesp.fct.evcomp.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessoes")
public class Sessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(nullable = false)
    private boolean ativa;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
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
        return false;
    }

    public void invalidarSessao() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
