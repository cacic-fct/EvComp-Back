package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.repository.ParticipanteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cadastro")

public class CadastroController {

    private final ParticipanteRepository participanteRepository;
    @Autowired
    public CadastroController(ParticipanteRepository participanteRepository) {
        this.participanteRepository = participanteRepository;
    }

    @PostMapping
    public ResponseEntity<?> confirmarCadastro(@RequestBody Map<String, String> payload) {
        String nome = payload.get("nome");
        String email = payload.get("email");
        String senha = payload.get("senha");
        String ra = payload.get("ra");

        if (participanteRepository.verificarEmailCadastrado(email)) {
            return ResponseEntity.status(400).body(Map.of("error", "Erro: Este e-mail já está em uso."));
        }

        try {
            String senhaHash = org.mindrot.jbcrypt.BCrypt.hashpw(senha, org.mindrot.jbcrypt.BCrypt.gensalt());
            Participante p = Participante.criarParticipante(nome, "", email, senhaHash);

            if (ra != null && !ra.isEmpty()) {
                p.setRA(ra);
            }

            participanteRepository.salvarNovoParticipante(p);

            return ResponseEntity.ok().body(Map.of("message", "Cadastro realizado com sucesso"));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            System.err.println("Erro de violação de integridade (e-mail ou RA duplicado): " + e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", "Erro de integridade. Este e-mail ou RA já pode estar em uso."));
        } catch (Exception e) {
            System.err.println("Erro desconhecido ao cadastrar usuário: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ocorreu um erro interno no servidor. Tente novamente mais tarde."));
        }
    }
}
