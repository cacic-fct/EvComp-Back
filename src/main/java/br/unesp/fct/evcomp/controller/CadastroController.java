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
        confirmarCadastro(payload.get("nome"), payload.get("email"), payload.get("senha"), payload.get("ra"));
        return ResponseEntity.ok().body(Map.of("message", "Cadastro realizado com sucesso"));
    }

    public void confirmarCadastro(String nome, String email, String senha, String ra) {
        Participante p = new Participante(nome, email, senha);
        if (ra != null && !ra.isEmpty()) {
            p.setRA(ra);
        }
        participanteRepository.save(p);
    }
}
