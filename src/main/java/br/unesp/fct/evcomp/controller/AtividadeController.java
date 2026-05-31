package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.domain.Atividade;
import br.unesp.fct.evcomp.domain.Evento;

import br.unesp.fct.evcomp.repository.AtividadeRepository;
import br.unesp.fct.evcomp.repository.EventoRepository;
import br.unesp.fct.evcomp.repository.InscricaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/atividades")
@CrossOrigin(origins = "*")
public class AtividadeController {

    private final AtividadeRepository atividadeRepository;
    private final EventoRepository eventoRepository;
    private final InscricaoRepository inscricaoRepository;
    private final br.unesp.fct.evcomp.service.AtividadeService atividadeService;

    @Autowired
    public AtividadeController(AtividadeRepository atividadeRepository, EventoRepository eventoRepository, InscricaoRepository inscricaoRepository, br.unesp.fct.evcomp.service.AtividadeService atividadeService) {
        this.atividadeRepository = atividadeRepository;
        this.eventoRepository = eventoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeService = atividadeService;
    }

    @GetMapping
    public ResponseEntity<List<Atividade>> listarAtividades() {
        return ResponseEntity.ok(atividadeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarAtividadeWeb(@PathVariable Integer id) {
        Optional<Atividade> atividade = atividadeRepository.findById(id);
        if (atividade.isPresent()) {
            return ResponseEntity.ok(atividade.get());
        }
        return ResponseEntity.status(404).body(Map.of("error", "Atividade não encontrada"));
    }

    @GetMapping("/{id}/selecionar")
    public ResponseEntity<?> selecionarAtividade(@PathVariable Integer id) {
        Optional<Atividade> atividadeOpt = atividadeService.buscarAtividade(id);
        
        if (atividadeOpt.isPresent()) {
            Atividade atividade = atividadeOpt.get();
            boolean periodoValido = atividadeService.checarPeriodoPresenca(atividade);
            
            if (periodoValido) {
                return ResponseEntity.ok(atividade);
            } else {
                return ResponseEntity.status(403).body(Map.of("error", "Atividade fora do período válido de registro de presença"));
            }
        }
        
        return ResponseEntity.status(404).body(Map.of("error", "Atividade não encontrada"));
    }

    public boolean edicaoAtividade(String atividadeId, Object novosDadosAtividade) { return false; }
    public Atividade buscarAtividade(String atividadeId) { return null; }
    public void excluirAtividade(String atividadeId) {}
    public void verificarVagas(String atividadeId) {}
    public void carregarEvento(String eventoId) {}
    public void confimarCriacao(String titulo, java.util.Date data_inicio, int horario_inicio, java.util.Date data_termino, int horario_termino, int max_participantes, br.unesp.fct.evcomp.domain.Participante ministrantes, int carga_horaria_ministrantes) {}
}
