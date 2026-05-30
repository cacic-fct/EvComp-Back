package br.unesp.fct.seccomp.controller;

import br.unesp.fct.seccomp.domain.Atividade;

import br.unesp.fct.seccomp.domain.Participante;
import br.unesp.fct.seccomp.domain.RegistroDePresenca;
import br.unesp.fct.seccomp.repository.AtividadeRepository;
import br.unesp.fct.seccomp.repository.InscricaoRepository;
import br.unesp.fct.seccomp.repository.ParticipanteRepository;
import br.unesp.fct.seccomp.repository.PresencaRepository;
import br.unesp.fct.seccomp.service.PresencaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/presenca")
@CrossOrigin(origins = "*")
public class PresencaController {

    private final AtividadeRepository atividadeRepository;
    private final ParticipanteRepository participanteRepository;
    private final InscricaoRepository inscricaoRepository;
    private final PresencaRepository presencaRepository;
    private final PresencaService presencaService;

    @Autowired
    public PresencaController(AtividadeRepository atividadeRepository, ParticipanteRepository participanteRepository, InscricaoRepository inscricaoRepository, PresencaRepository presencaRepository, PresencaService presencaService) {
        this.atividadeRepository = atividadeRepository;
        this.participanteRepository = participanteRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.presencaRepository = presencaRepository;
        this.presencaService = presencaService;
    }

    public void registrarPresenca(String atividadeId, String codigoParticipante) {}
}
