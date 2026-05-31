package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.repository.ParticipanteRepository;
import br.unesp.fct.evcomp.service.ValidacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cadastro")
@CrossOrigin(origins = "*")
public class CadastroController {

    private final ParticipanteRepository participanteRepository;
    private final ValidacaoService validacaoService;

    @Autowired
    public CadastroController(ParticipanteRepository participanteRepository, ValidacaoService validacaoService) {
        this.participanteRepository = participanteRepository;
        this.validacaoService = validacaoService;
    }

    @PostMapping
    public ResponseEntity<?> confirmarCadastroWeb(@RequestBody Map<String, String> payload) {
        try {
            confirmarCadastro(payload.get("nome"), payload.get("email"), payload.get("senha"), payload.get("ra"));
            return ResponseEntity.ok().body(Map.of("message", "Cadastro realizado com sucesso"));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            System.err.println("Erro de violação de integridade (e-mail ou RA duplicado): " + e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", "Erro de integridade. Este e-mail ou RA já pode estar em uso."));
        } catch (Exception e) {
            System.err.println("Erro desconhecido ao cadastrar usuário:");
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno no servidor: " + e.getMessage()));
        }
    }

    public void confirmarCadastro(String nome, String email, String senha, String ra) {
        // Se o nome vier com espaço, podemos tentar separar nome e sobrenome, 
        // mas por enquanto passaremos o nome completo no primeiro campo e vazio no segundo
        String senhaHash = org.mindrot.jbcrypt.BCrypt.hashpw(senha, org.mindrot.jbcrypt.BCrypt.gensalt());
        Participante p = new Participante(nome, "", email, senhaHash);
        if (ra != null && !ra.isEmpty()) {
            p.setRA(ra);
        }
        participanteRepository.save(p);
    }
}
