package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.dto.CadastroRequestDTO;
import br.unesp.fct.evcomp.repository.ParticipanteRepository;

import jakarta.validation.Valid;
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
    public ResponseEntity<?> confirmarCadastro(@Valid @RequestBody CadastroRequestDTO payload) {
        String nomeCompleto = payload.getNome();
        String email = payload.getEmail();
        String senha = payload.getSenha();
        String ra = payload.getRa();

        if (participanteRepository.verificarEmailCadastrado(email)) {
            return ResponseEntity.status(400).body(Map.of("error", "Erro: Este e-mail já está em uso."));
        }

        try {
            String senhaHash = org.mindrot.jbcrypt.BCrypt.hashpw(senha, org.mindrot.jbcrypt.BCrypt.gensalt());
            Participante novoParticipante = Participante.criarParticipante(nomeCompleto, email, senhaHash);

            if (ra != null && !ra.isEmpty()) {
                novoParticipante.setRA(ra);
            }

            participanteRepository.salvarNovoParticipante(novoParticipante);

            return ResponseEntity.ok().body(Map.of("message", "Cadastro realizado com sucesso"));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(400).body(Map.of("error", "Erro de integridade. Este e-mail ou RA já pode estar em uso."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ocorreu um erro interno no servidor. Tente novamente mais tarde."));
        }
    }
}

