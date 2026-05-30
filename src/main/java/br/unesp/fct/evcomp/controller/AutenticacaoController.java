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
        
        Optional<br.unesp.fct.evcomp.domain.Usuário> userOpt = usuarioRepository.buscarUsuarioPorEmail(email);
        
        if (userOpt.isPresent() && userOpt.get().getSenha().equals(senha)) {
            return ResponseEntity.ok(Map.of("message", "Login bem-sucedido", "nome", userOpt.get().getNome(), "role", userOpt.get().getClass().getSimpleName().toUpperCase()));
        }
        
        return ResponseEntity.status(401).body(Map.of("error", "Credenciais Inválidas"));
    }

    public void confirmarLogin(String email, String senha) {
    }

    private void validarCredenciais(String email, String senha) {
    }

    public boolean encerrarSessao() {
        return false;
    }

    public void removerDadosAutenticacao() {
    }
}
