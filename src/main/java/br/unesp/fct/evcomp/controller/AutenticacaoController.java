package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.domain.Sessao;

import br.unesp.fct.evcomp.domain.Administrador;
import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.repository.SessaoRepository;
import br.unesp.fct.evcomp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AutenticacaoController {

    private final UsuarioRepository usuarioRepository;
    private final SessaoRepository sessaoRepository;

    @Autowired
    public AutenticacaoController(UsuarioRepository usuarioRepository, SessaoRepository sessaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.sessaoRepository = sessaoRepository;
    }

    @PostMapping
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String senha = payload.get("senha");
        return confirmarLogin(email, senha);
    }

    public ResponseEntity<?> confirmarLogin(String email, String senha) {
        return validarCredenciais(email, senha);
    }

    private ResponseEntity<?> validarCredenciais(String email, String senha) {
        Optional<br.unesp.fct.evcomp.domain.Usuário> userOpt = usuarioRepository.buscarUsuarioPorEmail(email);
        
        if (userOpt.isPresent()) {
            br.unesp.fct.evcomp.domain.Usuário usuarioExiste = userOpt.get();
            boolean senhaValida = usuarioExiste.validarSenha(senha, usuarioExiste);
            
            if (senhaValida) {
                Sessao novaSessao = new Sessao();
                boolean sessaoIniciada = novaSessao.iniciarSessao(usuarioExiste);
                
                if (sessaoIniciada) {
                    sessaoRepository.save(novaSessao);
                    boolean isColetor = false;
                    if (usuarioExiste instanceof br.unesp.fct.evcomp.domain.ColetorDePresenca) {
                         isColetor = !((br.unesp.fct.evcomp.domain.ColetorDePresenca) usuarioExiste).getEventosColetados().isEmpty();
                    }
                    return ResponseEntity.ok(Map.of(
                        "message", "Login bem-sucedido", 
                        "nome", usuarioExiste.getNome(), 
                        "role", usuarioExiste.getRole(),
                        "isColetor", String.valueOf(isColetor),
                        "token", novaSessao.getToken()
                    ));
                }
            }
        }
        
        return ResponseEntity.status(401).body(Map.of("error", "Credenciais Inválidas"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(400).body(Map.of("error", "Token não fornecido."));
        }
        String token = authHeader.substring(7);
        boolean logoutRealizado = encerrarSessao(token);
        
        if (logoutRealizado) {
            return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso. Credenciais de acesso da sessão invalidadas."));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Sessão não encontrada ou já expirada."));
    }

    public boolean encerrarSessao(String token) {
        Optional<Sessao> sessaoOpt = sessaoRepository.buscarSessaoPorToken(token);
        if (sessaoOpt.isPresent()) {
            Sessao sessao = sessaoOpt.get();
            sessao.invalidarSessao();
            sessaoRepository.save(sessao);
            removerDadosAutenticacao();
            return true;
        }
        return false;
    }

    public void removerDadosAutenticacao() {
        // Lógica adicional de remoção de dados caso necessário (limpar caches, etc.)
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Token não fornecido ou inválido."));
        }
        
        String token = authHeader.substring(7);
        Optional<Sessao> sessaoOpt = sessaoRepository.buscarSessaoPorToken(token);
        
        if (sessaoOpt.isPresent() && sessaoOpt.get().isAtiva()) {
            br.unesp.fct.evcomp.domain.Usuário user = sessaoOpt.get().getUsuario();
            Map<String, String> userData = new java.util.HashMap<>();
            userData.put("id", String.valueOf(user.getId()));
            userData.put("nome", user.getNome());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole());
            if (user instanceof br.unesp.fct.evcomp.domain.Participante) {
                String ra = ((br.unesp.fct.evcomp.domain.Participante) user).getRA();
                userData.put("ra", ra != null ? ra : "");
                boolean isColetor = false;
                if (user instanceof br.unesp.fct.evcomp.domain.ColetorDePresenca) {
                    isColetor = !((br.unesp.fct.evcomp.domain.ColetorDePresenca) user).getEventosColetados().isEmpty();
                }
                userData.put("isColetor", String.valueOf(isColetor));
            } else {
                userData.put("isColetor", "false");
            }
            return ResponseEntity.ok(userData);
        }
        
        return ResponseEntity.status(401).body(Map.of("error", "Sessão inválida ou expirada."));
    }


}
