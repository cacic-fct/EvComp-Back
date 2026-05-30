package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.domain.Atividade;
import br.unesp.fct.evcomp.domain.Evento;

import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.repository.AtividadeRepository;
import br.unesp.fct.evcomp.repository.EventoRepository;
import br.unesp.fct.evcomp.repository.InscricaoRepository;
import br.unesp.fct.evcomp.repository.ParticipanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/inscricoes")
@CrossOrigin(origins = "*")
public class InscricaoController {

    private final InscricaoRepository inscricaoRepository;
    private final ParticipanteRepository participanteRepository;
    private final EventoRepository eventoRepository;
    private final AtividadeRepository atividadeRepository;

    @Autowired
    public InscricaoController(InscricaoRepository inscricaoRepository, ParticipanteRepository participanteRepository, EventoRepository eventoRepository, AtividadeRepository atividadeRepository) {
        this.inscricaoRepository = inscricaoRepository;
        this.participanteRepository = participanteRepository;
        this.eventoRepository = eventoRepository;
        this.atividadeRepository = atividadeRepository;
    }

    public void inscreverParticipante(String participanteId, String eventoId, br.unesp.fct.evcomp.domain.Atividade atividades) {}
}
