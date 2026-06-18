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

    @Autowired
    private br.unesp.fct.evcomp.controller.AtividadeController atividadeController;

    public ResponseEntity<?> inscreverParticipante(Integer participanteId, Integer eventoId, List<Integer> atividades) {
        if (participanteId == null || eventoId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Participante e Evento são obrigatórios."));
        }

        // Por segurança extra não contida no diagrama de confirmar inscrição, checamos se Part. e Ev. existem.
        Inscricao inscricao = inscricaoRepository.buscarPorParticipanteEEvento(participanteId, eventoId);

        Optional<Participante> participante = participanteRepository.buscarParticipantePorId(participanteId);
        Optional<Evento> evento = eventoRepository.buscarEventoPorId(eventoId);

        if (!inscricao) {
            return ResponseEntity.status(404).body(Map.of("error", "Participante ou Evento não encontrado."));
        }

        // O que fazer aqui?
        if (atividades == null || atividades.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Selecione pelo menos uma atividade para concluir a inscrição no evento."));
        }

        inscricaoRepository.salvarInscricao(inscricao);

        // List<Atividade> atividadesObjetos = new ArrayList<>();
        
        // // --- Delegação para AtividadeController (Diagrama 3) ---
        // for (Integer atvId : atividades) {
        //     Optional<Atividade> aOpt = atividadeRepository.findById(atvId);
        //     if (aOpt.isPresent()) {
        //         Atividade a = aOpt.get();

        //         // 2: verificarVagas(atividadeId)
        //         ResponseEntity<?> responseVagas = atividadeController.verificarVagas(String.valueOf(atvId));
        //         Map<String, Object> bodyVagas = (Map<String, Object>) responseVagas.getBody();
        //         if ((Integer) bodyVagas.get("vagasDisponiveis") <= 0) {
        //             return ResponseEntity.badRequest().body(Map.of("error", "A atividade '" + a.getTitulo() + "' não possui vagas disponíveis."));
        //         }
                
        //         // Validação de Cronologia mantida para segurança
        //         if (a.getDataInicio() != null && a.getHorarioInicio() != null) {
        //             LocalDateTime inicioAtividade = a.getDataInicio().atTime(a.getHorarioInicio());
        //             if (LocalDateTime.now().isAfter(inicioAtividade)) {
        //                 return ResponseEntity.badRequest().body(Map.of("error", "A atividade '" + a.getTitulo() + "' já foi iniciada ou encerrada."));
        //             }
        //         }
        //         atividadesObjetos.add(a);
        //     }
        // }
        
        // // 3: verificarConflitos(atividades, atividadeId)
        // for (Integer atvId : atividades) {
        //     ResponseEntity<?> responseConflitos = atividadeController.verificarConflitos(atividades, atvId);
        //     Map<String, Object> bodyConflitos = (Map<String, Object>) responseConflitos.getBody();
        //     if ((Boolean) bodyConflitos.get("conflitoDetectado")) {
        //         return ResponseEntity.badRequest().body(Map.of("error", bodyConflitos.get("mensagem")));
        //     }
        // }

        // // --- Delegação para InscricaoRepository (Diagrama 4) ---
        // // 1: confirmarInscricao(participanteId, eventoId, atividades) -> 2: inscreverParticipante
        // // Checamos a dupla inscrição:
        // br.unesp.fct.evcomp.domain.Inscrição inscricaoExistente = inscricaoRepository.buscarPorParticipanteEEvento(participanteId, eventoId);
        // if (inscricaoExistente != null && inscricaoExistente.isStatus()) {
        //     return ResponseEntity.badRequest().body(Map.of("error", "Participante já inscrito neste evento."));
        // }

        // // Salvar a Inscrição delegando ao Repositório
        // br.unesp.fct.evcomp.domain.Inscrição inscricaoEfetuada = inscricaoRepository.inscreverParticipante(
        //     participanteId, 
        //     eventoId, 
        //     atividadesObjetos, 
        //     pOpt.get(), 
        //     eOpt.get()
        // );

        // return ResponseEntity.ok(inscricaoEfetuada);
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
