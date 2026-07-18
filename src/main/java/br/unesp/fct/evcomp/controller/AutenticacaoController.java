package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.config.JwtUtil;
import br.unesp.fct.evcomp.dto.LoginRequestDTO;
import br.unesp.fct.evcomp.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/auth")

public class AutenticacaoController {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public AutenticacaoController(UsuarioRepository usuarioRepository, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO payload) {
        String email = payload.getEmail();
        String senha = payload.getSenha();

        Optional<br.unesp.fct.evcomp.domain.Usuário> userOpt = usuarioRepository.buscarUsuarioPorEmail(email);
        
        if (userOpt.isPresent()) {
            br.unesp.fct.evcomp.domain.Usuário usuario = userOpt.get();
            boolean senhaValida = usuario.validarSenha(senha, usuario);
            
            if (senhaValida) {
                boolean isColetor = false;
                if (usuario instanceof br.unesp.fct.evcomp.domain.ColetorDePresenca) {
                    isColetor = !((br.unesp.fct.evcomp.domain.ColetorDePresenca) usuario).getEventosColetados().isEmpty();
                }

                // Gera o token JWT contendo os dados do usuário (stateless, sem banco)
                String token = jwtUtil.gerarToken(
                    usuario.getId(),
                    usuario.getEmail(),
                    usuario.getNomeCompleto(),
                    usuario.getRole(),
                    isColetor
                );

                return ResponseEntity.ok(Map.of(
                    "message", "Login bem-sucedido", 
                    "nome", usuario.getNomeCompleto(), 
                    "role", usuario.getRole(),
                    "isColetor", String.valueOf(isColetor),
                    "token", token
                ));
            }
        }
        
        return ResponseEntity.status(401).body(Map.of("error", "Credenciais Inválidas"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Com JWT stateless, o logout é responsabilidade do frontend (descartar o token).
        // O servidor não precisa invalidar nada no banco de dados.
        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso. Credenciais de acesso da sessão invalidadas."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Token não fornecido ou inválido."));
        }

        String token = authHeader.substring(7);
        Claims claims = jwtUtil.validarToken(token);

        if (claims != null) {
            // Todos os dados do usuário são extraídos do JWT — ZERO consultas ao banco!
            Map<String, String> userData = new HashMap<>();
            userData.put("id", claims.getSubject());
            userData.put("nome", jwtUtil.extrairNome(claims));
            userData.put("email", jwtUtil.extrairEmail(claims));
            userData.put("role", jwtUtil.extrairRole(claims));
            userData.put("isColetor", String.valueOf(jwtUtil.extrairIsColetor(claims)));

            return ResponseEntity.ok(userData);
        }

        return ResponseEntity.status(401).body(Map.of("error", "Token inválido ou expirado."));
    }
}
