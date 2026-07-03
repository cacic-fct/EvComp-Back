package br.unesp.fct.evcomp.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import br.unesp.fct.evcomp.repository.UsuarioRepository;
import br.unesp.fct.evcomp.service.SistemaEmail;
import br.unesp.fct.evcomp.domain.TokenRedefinicao;
import br.unesp.fct.evcomp.domain.Usuário;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/redefinicao-senha")

public class RedefinicaoSenhaController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SistemaEmail sistemaEmail;
    
    @Autowired
    private br.unesp.fct.evcomp.service.RedefinicaoSenhaService redefinicaoSenhaService;

    @PostMapping("/solicitar")
    public ResponseEntity<Map<String, String>> confirmarRedefinicao(@RequestParam String email) {
        Optional<Usuário> usuarioExiste = usuarioRepository.validarEmailCadastrado(email);

        if (usuarioExiste.isPresent()) {
            Usuário user = usuarioExiste.get();
            TokenRedefinicao.invalidarTokensDoUsuario(user.getId());
            TokenRedefinicao tokenGerado = TokenRedefinicao.gerarToken();

            tokenGerado.setUsuario(user);

            sistemaEmail.enviarEmailRedefinicao(email, tokenGerado.getTokenGerado());
        }

        Map<String, String> response = new HashMap<>();

        response.put("message", "Instruções enviadas com sucesso.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/validar")
    public ResponseEntity<Boolean> confirmarToken(@RequestParam int tokenRecebido) {
        Optional<TokenRedefinicao> tokenEncontrado = TokenRedefinicao.buscarPorToken(tokenRecebido);

        if (tokenEncontrado.isPresent()) {
            TokenRedefinicao tokenGerado = tokenEncontrado.get();

            boolean ehValido = tokenGerado.validarToken(tokenRecebido, tokenGerado);

            if (ehValido) {
                return ResponseEntity.ok(true);
            }
        }
        return ResponseEntity.ok(false);
    }

    @PostMapping("/confirmar")
    public ResponseEntity<Boolean> confirmarNovaSenha(@RequestParam int tokenRecebido, @RequestParam String novaSenha) {
        if (!redefinicaoSenhaService.validarSenha(novaSenha)) {
            return ResponseEntity.badRequest().body(false); // Retorna falso indicando que a senha não atende aos padrões
        }
        
        Optional<TokenRedefinicao> tokenEncontrado = TokenRedefinicao.buscarPorToken(tokenRecebido);

        if (tokenEncontrado.isPresent()) {
            TokenRedefinicao tokenGerado = tokenEncontrado.get();

            if (tokenGerado.validarToken(tokenRecebido, tokenGerado)) {
                Usuário user = tokenGerado.getUsuario();
                String hashSenha = BCrypt.hashpw(novaSenha, BCrypt.gensalt());
                boolean senhaAtualizada = usuarioRepository.atualizarSenha(hashSenha, user.getEmail());

                if (senhaAtualizada) {
                    tokenGerado.invalidarToken();

                    return ResponseEntity.ok(true);
                }
            }
        }

        return ResponseEntity.ok(false);
    }
}
