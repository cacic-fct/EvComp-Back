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

    @PostMapping
    public ResponseEntity<?> inscreverParticipanteWeb(@RequestBody Map<String, Object> req) {
        try {
            String participanteId = String.valueOf(req.get("participanteId"));
            String eventoId = String.valueOf(req.get("eventoId"));
            List<Integer> atividadeIds = (List<Integer>) req.get("atividadeIds");
            return inscreverParticipante(participanteId, eventoId, atividadeIds);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Erro ao processar a requisição: " + e.getMessage()));
        }
    }

    public ResponseEntity<?> inscreverParticipante(String participanteId, String eventoId, List<Integer> atividadesIds) {
        if (participanteId == null || eventoId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Participante e Evento são obrigatórios."));
        }

        if (inscricaoRepository.buscarPorParticipanteEEvento(participanteId, eventoId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Participante já inscrito neste evento."));
        }

        if (eventoRepository.checarAndamentoEvento(eventoId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Evento já encerrado."));
        }

        Optional<Participante> pOpt = participanteRepository.findById(Integer.valueOf(participanteId));
        Optional<Evento> eOpt = eventoRepository.buscarEventoPorIdInt(Integer.valueOf(eventoId));
        
        if (!pOpt.isPresent() || !eOpt.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("error", "Participante ou Evento não encontrado."));
        }

        List<Atividade> atividades = new ArrayList<>();
        if (atividadesIds != null) {
            for (Integer atvId : atividadesIds) {
                Optional<Atividade> aOpt = atividadeRepository.findById(atvId);
                if (aOpt.isPresent()) {
                    Atividade a = aOpt.get();
                    int maxVagas = a.getMaxParticipantes();
                    int inscritos = inscricaoRepository.contarInscritosPorAtividadeInt(atvId);
                    if (inscritos >= maxVagas) {
                        return ResponseEntity.badRequest().body(Map.of("error", "A atividade '" + a.getTitulo() + "' não possui vagas disponíveis."));
                    }
                    // Validate conflicts
                    for (Atividade atvAdicionada : atividades) {
                        if (atvAdicionada.verificarConflitoHorarios(a)) {
                            return ResponseEntity.badRequest().body(Map.of("error", "Conflito de horários entre as atividades selecionadas."));
                        }
                    }
                    atividades.add(a);
                }
            }
        }

        br.unesp.fct.evcomp.domain.Inscrição inscricao = new br.unesp.fct.evcomp.domain.Inscrição(
            new java.util.Date(),
            true,
            pOpt.get(),
            eOpt.get(),
            atividades
        );

        inscricaoRepository.save(inscricao);
        return ResponseEntity.ok(inscricao);
    }
}
