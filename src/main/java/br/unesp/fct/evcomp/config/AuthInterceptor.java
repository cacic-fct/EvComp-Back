package br.unesp.fct.evcomp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @deprecated Substituído pelo {@link JwtAuthenticationFilter} + {@link SecurityConfig}.
 * 
 * Este interceptor foi o mecanismo original de autenticação/autorização do EvComp.
 * Ele consultava a tabela de Sessão no banco de dados em TODAS as requisições.
 * 
 * Agora a autenticação é feita via JWT stateless pelo Spring Security.
 * Este arquivo é mantido apenas para referência histórica.
 */
@Deprecated
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Não utilizado. A autenticação agora é feita pelo JwtAuthenticationFilter.
        return true;
    }
}
