package br.unesp.fct.seccomp.controller;


import br.unesp.fct.seccomp.repository.UsuarioRepository;
import br.unesp.fct.seccomp.service.ValidacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/redefinir-senha")
@CrossOrigin(origins = "*")
public class RedefinicaoSenhaController {

    private final UsuarioRepository usuarioRepository;
    private final ValidacaoService validacaoService;

    @Autowired
    public RedefinicaoSenhaController(UsuarioRepository usuarioRepository, ValidacaoService validacaoService) {
        this.usuarioRepository = usuarioRepository;
        this.validacaoService = validacaoService;
    }

    public void confirmarRedefinicao(String email) {}
    public void confirmarToken(int tokenRecebido) {}
}
