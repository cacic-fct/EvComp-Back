package br.unesp.fct.evcomp.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT que substitui o antigo AuthInterceptor.
 * Executa ANTES de cada requisição, validando o token JWT e configurando
 * o SecurityContext do Spring Security com as informações do usuário autenticado.
 * 
 * Também injeta os atributos `usuarioLogadoId` e `usuarioLogadoRole` na request
 * para manter compatibilidade com controllers que já usam esses atributos (ex: IDOR check).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extrairToken(request);

        if (token != null) {
            Claims claims = jwtUtil.validarToken(token);

            if (claims != null) {
                Integer userId = jwtUtil.extrairUserId(claims);
                String role = jwtUtil.extrairRole(claims);

                // Configura o Spring Security com a autenticação do usuário
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userId, // principal = userId
                    null,   // credentials (não necessário após autenticação)
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

                // Mantém compatibilidade com controllers que usam request.getAttribute
                request.setAttribute("usuarioLogadoId", userId);
                request.setAttribute("usuarioLogadoRole", role);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o token JWT do header Authorization (Bearer) ou do cookie auth_token.
     */
    private String extrairToken(HttpServletRequest request) {
        // Tenta pelo header Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Tenta pelo cookie auth_token (compatibilidade com frontend)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
