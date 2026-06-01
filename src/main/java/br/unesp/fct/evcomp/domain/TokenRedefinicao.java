package br.unesp.fct.evcomp.domain;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class TokenRedefinicao {

    // Simulação em memória já que TokenRedefinicao não deve ir para o banco
    private static final Map<Integer, TokenRedefinicao> tokensEmMemoria = new HashMap<>();

    private int tokenGerado;
    private LocalDateTime dataExpiracao;
    private boolean utilizado = false;
    private Usuário usuario;

    public TokenRedefinicao() {
    }

    public TokenRedefinicao gerarToken() {
        Random random = new Random();
        this.tokenGerado = 100000 + random.nextInt(900000); // 6 digits token
        this.dataExpiracao = LocalDateTime.now().plusHours(1); // 1 hour validity
        this.utilizado = false;
        
        // Salva na memória
        tokensEmMemoria.put(this.tokenGerado, this);
        return this;
    }

    public boolean validarToken(int tokenRecebido, TokenRedefinicao tokenGeradoObj) {
        if (this.utilizado) return false;
        if (LocalDateTime.now().isAfter(this.dataExpiracao)) return false;
        return this.tokenGerado == tokenRecebido;
    }

    public void invalidarToken() {
        this.utilizado = true;
    }

    public static Optional<TokenRedefinicao> findByToken(int token) {
        return Optional.ofNullable(tokensEmMemoria.get(token));
    }

    public int getTokenGerado() {
        return tokenGerado;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public boolean isUtilizado() {
        return utilizado;
    }

    public Usuário getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuário usuario) {
        this.usuario = usuario;
    }
}
