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
            Integer participanteId = Integer.valueOf(String.valueOf(req.get("participanteId")));
            Integer eventoId = Integer.valueOf(String.valueOf(req.get("eventoId")));
            List<Integer> atividades = (List<Integer>) req.get("atividadeIds");

            return inscreverParticipante(participanteId, eventoId, atividades);
        } catch (Exception e) {
            System.err.println("Erro processar inscricao: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Erro interno ao processar a inscrição. Tente novamente."));
        }
    }

    public ResponseEntity<?> inscreverParticipante(Integer participanteId, Integer eventoId, List<Integer> atividades) {
        if (participanteId == null || eventoId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Participante e Evento são obrigatórios."));
        }

        Optional<Participante> participante = participanteRepository.buscarParticipantePorId(participanteId);
        Optional<Evento> evento = eventoRepository.buscarEventoPorId(eventoId);

        if (!participante.isPresent() || !evento.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("error", "Participante ou Evento não encontrado."));
        }

        // Resgata as atividades do evento e filtra apenas as selecionadas
        List<Atividade> todasAtividades = atividadeRepository.buscarAtividadesPorEvento(eventoId);
        List<Atividade> atividadesObjetos = todasAtividades.stream()
            .filter(a -> atividades.contains(a.getId()))
            .toList();

        if (atividadesObjetos.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nenhuma atividade válida selecionada."));
        }

        // Busca a Inscrição Atual
        Optional<br.unesp.fct.evcomp.domain.Inscrição> inscricaoExistente = inscricaoRepository.buscarPorParticipanteEEvento(participanteId, eventoId);

        br.unesp.fct.evcomp.domain.Inscrição inscricao;
        if (inscricaoExistente.isPresent()) {
            inscricao = inscricaoExistente.get();
            if (inscricao.isStatus()) { // Já inscrito e ativo
                return ResponseEntity.badRequest().body(Map.of("error", "Participante já inscrito neste evento."));
            }
            // Reativa a inscrição cancelada
            inscricao.setStatus(true);
            inscricao.setAtividade(atividadesObjetos);
            inscricao.setDataInscricao(LocalDateTime.now());
        } else {
            // Nova Inscrição
            inscricao = new br.unesp.fct.evcomp.domain.Inscrição(
                LocalDateTime.now(), true, participante.get(), evento.get(), atividadesObjetos
            );
        }

        inscricaoRepository.salvarInscricao(inscricao);

        return ResponseEntity.ok(inscricao);
    }

    @GetMapping("/minhas")
    public ResponseEntity<?> listarEventosInscritos(@RequestParam("participanteId") String participanteId) {
        try {
            List<Integer> eventosIds = inscricaoRepository.buscarEventosInscritosPorParticipante(Integer.valueOf(participanteId));

            return ResponseEntity.ok(Map.of("inscritos", eventosIds));
        } catch (Exception e) {
            System.err.println("Erro listarEventosInscritos: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Ocorreu um erro ao listar os eventos inscritos."));
        }
    }

    @GetMapping("/detalhes")
    public ResponseEntity<?> listarInscricoesDetalhadas(@RequestParam("participanteId") String participanteId) {
        try {
            List<br.unesp.fct.evcomp.domain.Inscrição> inscricoes = inscricaoRepository.buscarInscricoesAtivasPorParticipante(Integer.valueOf(participanteId));
            return ResponseEntity.ok(inscricoes);
        } catch (Exception e) {
            System.err.println("Erro listarInscricoesDetalhadas: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Ocorreu um erro ao carregar os detalhes de inscrições."));
        }
    }
}
