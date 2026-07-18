package br.unesp.fct.evcomp.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Utilitário para geração e validação de tokens JWT (JSON Web Tokens).
 * Substitui a tabela de Sessão no banco de dados por tokens stateless.
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
        @Value("${jwt.secret:EvCompSecretKeyParaDesenvolvimentoLocal2026!MinimoDeVinteECincoCaracteres}") String secret,
        @Value("${jwt.expiration-ms:86400000}") long expirationMs // 24 horas por padrão
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Gera um token JWT contendo os dados do usuário autenticado.
     */
    public String gerarToken(Integer userId, String email, String nome, String role, boolean isColetor) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(Map.of(
                    "email", email,
                    "nome", nome,
                    "role", role,
                    "isColetor", isColetor
                ))
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(key)
                .compact();
    }

    /**
     * Valida o token JWT e retorna os Claims (dados) contidos nele.
     * Retorna null se o token for inválido ou expirado.
     */
    public Claims validarToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public Integer extrairUserId(Claims claims) {
        return Integer.parseInt(claims.getSubject());
    }

    public String extrairRole(Claims claims) {
        return claims.get("role", String.class);
    }

    public String extrairEmail(Claims claims) {
        return claims.get("email", String.class);
    }

    public String extrairNome(Claims claims) {
        return claims.get("nome", String.class);
    }

    public boolean extrairIsColetor(Claims claims) {
        return claims.get("isColetor", Boolean.class);
    }
}
