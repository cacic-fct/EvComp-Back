package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.repository.ParticipanteRepository;
import br.unesp.fct.evcomp.service.ValidacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/participantes")
@CrossOrigin(origins = "*")
public class ParticipanteController {

    private final ParticipanteRepository participanteRepository;
    private final ValidacaoService validacaoService;

    @Autowired
    public ParticipanteController(ParticipanteRepository participanteRepository, ValidacaoService validacaoService) {
        this.participanteRepository = participanteRepository;
        this.validacaoService = validacaoService;
    }

    public void buscarParticipante(String participanteId) {
    }

    public void editarParticipante(String participanteId, String nome, String ra) {
    }
}
