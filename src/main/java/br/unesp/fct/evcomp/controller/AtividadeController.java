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
    private final br.unesp.fct.evcomp.repository.ParticipanteRepository participanteRepository;

    @Autowired
    public AtividadeController(AtividadeRepository atividadeRepository, EventoRepository eventoRepository, InscricaoRepository inscricaoRepository, br.unesp.fct.evcomp.service.AtividadeService atividadeService, br.unesp.fct.evcomp.repository.ParticipanteRepository participanteRepository) {
        this.atividadeRepository = atividadeRepository;
        this.eventoRepository = eventoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeService = atividadeService;
        this.participanteRepository = participanteRepository;
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

    @PostMapping("/evento/{eventoId}")
    public ResponseEntity<?> criarAtividadeWeb(@PathVariable Integer eventoId, @RequestBody Map<String, String> req) {
        Optional<Evento> ev = eventoRepository.buscarEventoPorIdInt(eventoId);
        if (!ev.isPresent()) return ResponseEntity.status(404).body(Map.of("error", "Evento não encontrado."));

        try {
            java.util.Date dataInicio = null;
            java.util.Date dataTermino = null;
            if (req.get("data_inicio") != null && !req.get("data_inicio").isEmpty()) {
                dataInicio = java.util.Date.from(LocalDate.parse(req.get("data_inicio")).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            }
            if (req.get("data_termino") != null && !req.get("data_termino").isEmpty()) {
                dataTermino = java.util.Date.from(LocalDate.parse(req.get("data_termino")).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            }
            int horInicio = req.get("horario_inicio") != null ? Integer.parseInt(req.get("horario_inicio")) : 0;
            int horFim = req.get("horario_termino") != null ? Integer.parseInt(req.get("horario_termino")) : 0;
            int maxP = req.get("max_participantes") != null ? Integer.parseInt(req.get("max_participantes")) : 0;
            int cargaH = req.get("carga_horaria_ministrantes") != null ? Integer.parseInt(req.get("carga_horaria_ministrantes")) : 0;

            br.unesp.fct.evcomp.domain.Participante ministrante = null;
            if (req.get("ministrante_id") != null && !req.get("ministrante_id").isEmpty()) {
                Optional<br.unesp.fct.evcomp.domain.Participante> pOpt = participanteRepository.findById(Integer.valueOf(req.get("ministrante_id")));
                if (pOpt.isPresent()) {
                    ministrante = pOpt.get();
                } else {
                    return ResponseEntity.status(404).body(Map.of("error", "Ministrante não encontrado no repositório."));
                }
            }

            return confirmarCriacao(req.get("titulo"), dataInicio, horInicio, dataTermino, horFim, maxP, ministrante, cargaH, ev.get());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Erro ao processar dados: " + e.getMessage()));
        }
    }

    public ResponseEntity<?> confirmarCriacao(String titulo, java.util.Date data_inicio, int horario_inicio, java.util.Date data_termino, int horario_termino, int max_participantes, br.unesp.fct.evcomp.domain.Participante ministrantes, int carga_horaria_ministrantes, Evento evento) {
        if (titulo == null || data_inicio == null || data_termino == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Campos obrigatórios ausentes."));
        }
        
        Atividade existente = atividadeRepository.verificarAtividadeCadastrada(titulo);
        if (existente != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Já existe uma atividade com este título."));
        }

        // Parse HHMM from int
        int h_inicio = horario_inicio / 100;
        int m_inicio = horario_inicio % 100;
        LocalTime timeInicio = LocalTime.of(h_inicio, m_inicio);

        int h_fim = horario_termino / 100;
        int m_fim = horario_termino % 100;
        LocalTime timeFim = LocalTime.of(h_fim, m_fim);

        java.util.Date dateHoraInicio = java.util.Date.from(timeInicio.atDate(LocalDate.now()).atZone(java.time.ZoneId.systemDefault()).toInstant());
        java.util.Date dateHoraFim = java.util.Date.from(timeFim.atDate(LocalDate.now()).atZone(java.time.ZoneId.systemDefault()).toInstant());

        // Assuming carga_horaria_total = carga_horaria_ministrantes for simplicity, or we can calculate difference
        int carga_horaria_total = carga_horaria_ministrantes; 
        
        Atividade atividade = new Atividade(titulo, data_inicio, dateHoraInicio, data_termino, dateHoraFim, max_participantes, carga_horaria_total, carga_horaria_ministrantes);
        // Fix dates for entity since setter uses LocalDate
        LocalDate ldInicio = data_inicio.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate ldTermino = data_termino.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        atividade.setDataInicio(ldInicio);
        atividade.setDataFim(ldTermino);
        
        atividade.setEvento(evento);
        if (ministrantes != null) {
            atividade.getMinistrantes().add(ministrantes);
        }
        
        atividadeRepository.save(atividade);
        return ResponseEntity.ok(atividade);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarAtividadeWeb(@PathVariable Integer id, @RequestBody Map<String, String> req) {
        return edicaoAtividade(String.valueOf(id), req);
    }

    public ResponseEntity<?> edicaoAtividade(String atividadeId, Map<String, String> req) {
        Optional<Atividade> atOpt = atividadeRepository.findById(Integer.valueOf(atividadeId));
        if (!atOpt.isPresent()) return ResponseEntity.status(404).body(Map.of("error", "Atividade não encontrada."));
        
        Atividade at = atOpt.get();
        String titulo = req.get("titulo");
        if (titulo != null && !titulo.equals(at.getTitulo()) && atividadeRepository.verificarAtividadeCadastrada(titulo) != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Já existe uma atividade com este título."));
        }
        if (titulo != null) at.setTitulo(titulo);
        if (req.get("max_participantes") != null) {
            at.setMaxParticipantes(Integer.parseInt(req.get("max_participantes")));
        }
        if (req.get("carga_horaria_ministrantes") != null) {
            at.setCargaHorariaMinistrante(Integer.parseInt(req.get("carga_horaria_ministrantes")));
            at.setCargaHorariaTotal(Integer.parseInt(req.get("carga_horaria_ministrantes")));
        }
        if (req.get("ministrante_id") != null && !req.get("ministrante_id").isEmpty()) {
            Optional<br.unesp.fct.evcomp.domain.Participante> pOpt = participanteRepository.findById(Integer.valueOf(req.get("ministrante_id")));
            if (pOpt.isPresent()) {
                at.getMinistrantes().clear();
                at.getMinistrantes().add(pOpt.get());
            }
        }
        
        atividadeRepository.save(at);
        return ResponseEntity.ok(at);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerAtividadeWeb(@PathVariable Integer id) {
        excluirAtividade(String.valueOf(id));
        return ResponseEntity.ok(Map.of("message", "Atividade excluída com sucesso."));
    }

    public void excluirAtividade(String atividadeId) {
        atividadeRepository.deleteById(Integer.valueOf(atividadeId));
    }

    @GetMapping("/{id}/vagas")
    public ResponseEntity<?> verificarVagasWeb(@PathVariable Integer id) {
        return verificarVagas(String.valueOf(id));
    }

    public ResponseEntity<?> verificarVagas(String atividadeId) {
        int maxVagas = atividadeRepository.findById(Integer.valueOf(atividadeId)).map(Atividade::getMaxParticipantes).orElse(0);
        int inscritos = inscricaoRepository.contarInscritosPorAtividadeInt(Integer.valueOf(atividadeId));
        return ResponseEntity.ok(Map.of("vagasDisponiveis", Math.max(0, maxVagas - inscritos)));
    }
}
