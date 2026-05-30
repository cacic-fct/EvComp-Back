package br.unesp.fct.seccomp.controller;

import br.unesp.fct.seccomp.domain.Sessao;

import br.unesp.fct.seccomp.domain.Administrador;
import br.unesp.fct.seccomp.domain.Participante;
import br.unesp.fct.seccomp.repository.SessaoRepository;
import br.unesp.fct.seccomp.repository.UsuarioRepository;
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
