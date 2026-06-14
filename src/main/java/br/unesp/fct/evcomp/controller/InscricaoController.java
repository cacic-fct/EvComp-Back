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
            String participanteId = String.valueOf(req.get("participanteId"));
            String eventoId = String.valueOf(req.get("eventoId"));
            List<Integer> atividades = (List<Integer>) req.get("atividadeIds");
            return inscreverParticipante(participanteId, eventoId, atividades);
        } catch (Exception e) {
            System.err.println("Erro processar inscricao: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Erro interno ao processar a inscrição. Tente novamente."));
        }
    }

    public ResponseEntity<?> inscreverParticipante(String participanteId, String eventoId, List<Integer> atividades) {
        if (participanteId == null || eventoId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Participante e Evento são obrigatórios."));
        }

        br.unesp.fct.evcomp.domain.Inscrição inscricaoExistente = inscricaoRepository.buscarPorParticipanteEEvento(participanteId, eventoId);
        if (inscricaoExistente != null && inscricaoExistente.isStatus()) {
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

        if (atividades == null || atividades.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Selecione pelo menos uma atividade para concluir a inscrição no evento."));
        }

        List<Atividade> atividadesObjetos = new ArrayList<>();
        for (Integer atvId : atividades) {
            Optional<Atividade> aOpt = atividadeRepository.findById(atvId);
                if (aOpt.isPresent()) {
                    Atividade a = aOpt.get();
                    int maxVagas = a.getMaxParticipantes();
                    int inscritos = inscricaoRepository.contarInscritosPorAtividadeInt(atvId);
                    if (inscritos >= maxVagas) {
                        return ResponseEntity.badRequest().body(Map.of("error", "A atividade '" + a.getTitulo() + "' não possui vagas disponíveis."));
                    }
                    
                    if (a.getDataInicio() != null && a.getHorarioInicio() != null) {
                        LocalDateTime inicioAtividade = a.getDataInicio().atTime(a.getHorarioInicio());
                        if (LocalDateTime.now().isAfter(inicioAtividade)) {
                            return ResponseEntity.badRequest().body(Map.of("error", "A atividade '" + a.getTitulo() + "' já foi iniciada ou encerrada."));
                        }
                    }

                    // Validate conflicts
                    for (Atividade atvAdicionada : atividadesObjetos) {
                        if (atvAdicionada.verificarConflitoHorarios(a)) {
                            return ResponseEntity.badRequest().body(Map.of("error", "Conflito de horários entre as atividades selecionadas."));
                        }
                    }
                    atividadesObjetos.add(a);
                }
            }

        br.unesp.fct.evcomp.domain.Inscrição inscricao;
        if (inscricaoExistente != null) {
            inscricao = inscricaoExistente;
            inscricao.setStatus(true);
            inscricao.setDataInscricao(LocalDateTime.now());
            inscricao.setAtividade(atividadesObjetos);
        } else {
            inscricao = new br.unesp.fct.evcomp.domain.Inscrição(
                LocalDateTime.now(),
                true,
                pOpt.get(),
                eOpt.get(),
                atividadesObjetos
            );
        }

        // Persistência
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
