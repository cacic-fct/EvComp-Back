package br.unesp.fct.evcomp.config;

import br.unesp.fct.evcomp.domain.Sessao;
import br.unesp.fct.evcomp.domain.Usuário;
import br.unesp.fct.evcomp.repository.SessaoRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private SessaoRepository sessaoRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // Se for requisição OPTIONS (Preflight do CORS), deixa passar
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        // Libera rotas públicas
        if (path.startsWith("/api/auth") || path.startsWith("/api/cadastro") || path.startsWith("/api/usuarios/cadastro") || path.startsWith("/error")) {
            return true;
        }

        // Tenta obter o token do cookie "auth_token" ou do header Authorization
        String token = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("auth_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Acesso negado. Token nao fornecido.\"}");
            response.setContentType("application/json");
            return false;
        }

        Optional<Sessao> sessaoOpt = sessaoRepository.buscarSessaoPorToken(token);
        
        if (sessaoOpt.isEmpty() || !sessaoOpt.get().isAtiva()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Acesso negado. Sessao invalida ou expirada.\"}");
            response.setContentType("application/json");
            return false;
        }

        Usuário usuario = sessaoOpt.get().getUsuario();
        request.setAttribute("usuarioLogadoId", usuario.getId());

        // Validação de Role para rotas administrativas
        if (path.startsWith("/api/relatorios") || 
           (path.startsWith("/api/eventos") && !request.getMethod().equalsIgnoreCase("GET")) ||
           (path.startsWith("/api/atividades") && !request.getMethod().equalsIgnoreCase("GET") && !path.endsWith("/selecionar")) ||
           (path.startsWith("/api/usuarios") && !request.getMethod().equalsIgnoreCase("GET") && !path.startsWith("/api/usuarios/cadastro"))) {
            
            if (!"ADMIN".equals(usuario.getRole())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"Acesso negado. Permissao de Administrador exigida.\"}");
                response.setContentType("application/json");
                return false;
            }
        }

        // Se chegou até aqui, está autenticado e autorizado!
        return true;
    }
}
