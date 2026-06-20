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

    @PostMapping
    public ResponseEntity<?> confirmarCriacao(@RequestBody Map<String, Object> req) {
        try {
            Integer eventoId = (Integer) req.get("evento_id");
            
            Evento evento = eventoRepository.buscarEventoPorId(eventoId).get();

            String titulo = String.valueOf(req.get("titulo"));
            
            Atividade atividadeJaCadastrada = atividadeRepository.verificarAtividadeCadastrada(titulo, eventoId);
            if (atividadeJaCadastrada != null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Já existe uma atividade com este título neste evento."));
            }

            LocalDate data_inicio = LocalDate.parse(String.valueOf(req.get("data_inicio")));
            LocalDate data_termino = LocalDate.parse(String.valueOf(req.get("data_termino")));
            
            LocalTime horario_inicio = LocalTime.parse(String.valueOf(req.get("horario_inicio")));
            LocalTime horario_termino = LocalTime.parse(String.valueOf(req.get("horario_termino")));
            
            int max_participantes = req.get("max_participantes") != null ? Integer.parseInt(String.valueOf(req.get("max_participantes"))) : 0;
            int carga_horaria_ministrantes = req.get("carga_horaria_ministrantes") != null ? Integer.parseInt(String.valueOf(req.get("carga_horaria_ministrantes"))) : 0;
            int carga_horaria_total = req.get("carga_horaria_total") != null ? Integer.parseInt(String.valueOf(req.get("carga_horaria_total"))) : 0;

            List<br.unesp.fct.evcomp.domain.Participante> ministrantes = new java.util.ArrayList<>();
            if (req.get("ministrantes_ids") != null) {
                List<Integer> ids = (List<Integer>) req.get("ministrantes_ids");
                if (!ids.isEmpty()) {
                    ministrantes = participanteRepository.buscarParticipantesPorId(ids);
                }
            }

            Atividade atividade = Atividade.criarAtividade(titulo, data_inicio, horario_inicio, data_termino, horario_termino, max_participantes, carga_horaria_total, ministrantes, carga_horaria_ministrantes);
            
            atividade.setEvento(evento);

            boolean salva = atividadeRepository.salvarAtividade(atividade);
            
            if(salva) {
                return ResponseEntity.ok(atividade);
            } else {
                return ResponseEntity.status(500).body(Map.of("error", "Erro interno ao salvar atividade."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Ocorreu um erro interno no servidor."));
        }
    }

    @org.springframework.transaction.annotation.Transactional
    @PutMapping("/{id}")
    public ResponseEntity<?> edicaoAtividade(@PathVariable Integer id, @RequestBody Map<String, Object> req) {
        Optional<Atividade> atOpt = atividadeRepository.findById(id);
        if (!atOpt.isPresent()) return ResponseEntity.status(404).body(Map.of("error", "Atividade não encontrada."));
        
        Atividade at = atOpt.get();
        String titulo = req.get("titulo") != null ? String.valueOf(req.get("titulo")) : null;
        if (titulo != null && !titulo.equals(at.getTitulo()) && atividadeRepository.buscarAtividadePorTitulo(titulo, at.getEvento().getId()).orElse(null) != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Já existe uma atividade com este título neste evento."));
        }
        
        if (titulo != null) at.setTitulo(titulo);
        if (req.get("max_participantes") != null) {
            int novoMax = Integer.parseInt(String.valueOf(req.get("max_participantes")));
            int inscritos = inscricaoRepository.contarInscritosPorAtividadeInt(id);
            if (novoMax < inscritos) {
                return ResponseEntity.badRequest().body(Map.of("error", "O número máximo de participantes não pode ser inferior aos já inscritos (" + inscritos + " inscritos atualmente)."));
            }
            at.setMaxParticipantes(novoMax);
        }
        if (req.get("carga_horaria_ministrantes") != null) {
            at.setCargaHorariaMinistrante(Integer.parseInt(String.valueOf(req.get("carga_horaria_ministrantes"))));
        }
        if (req.get("carga_horaria_total") != null) {
            at.setCargaHorariaTotal(Integer.parseInt(String.valueOf(req.get("carga_horaria_total"))));
        }
        
        if (req.get("ministrantes_ids") != null && req.get("ministrantes_ids") instanceof List) {
            List<Integer> ids = new java.util.ArrayList<>();
            for (Object idObj : (List<?>) req.get("ministrantes_ids")) {
                ids.add(Integer.parseInt(String.valueOf(idObj)));
            }
            List<br.unesp.fct.evcomp.domain.Participante> novosMinistrantes = participanteRepository.findAllById(ids);
            at.getMinistrantes().clear();
            at.getMinistrantes().addAll(novosMinistrantes);
        }
        
        // Handle dates parsing and validation on edit
        int desinscritos = 0;
        try {
            if (req.get("data_inicio") != null && !String.valueOf(req.get("data_inicio")).isEmpty()) {
                LocalDate atvInicio = LocalDate.parse(String.valueOf(req.get("data_inicio")));
                if (at.getEvento() != null && at.getEvento().getDataInicio() != null && atvInicio.isBefore(at.getEvento().getDataInicio())) {
                    return ResponseEntity.badRequest().body(Map.of("error", "A data de início da atividade não pode ser anterior à data de início do evento."));
                }
                at.setDataInicio(atvInicio);
            }
            if (req.get("data_termino") != null && !String.valueOf(req.get("data_termino")).isEmpty()) {
                LocalDate atvFim = LocalDate.parse(String.valueOf(req.get("data_termino")));
                if (at.getEvento() != null && at.getEvento().getDataFim() != null && atvFim.isAfter(at.getEvento().getDataFim())) {
                    return ResponseEntity.badRequest().body(Map.of("error", "A data de término da atividade não pode ser posterior à data de término do evento."));
                }
                at.setDataFim(atvFim);
            }
            
            if (req.get("horario_inicio") != null) {
                int h = Integer.parseInt(String.valueOf(req.get("horario_inicio")));
                at.setHorarioInicio(LocalTime.of(h / 100, h % 100));
            }
            if (req.get("horario_termino") != null) {
                int h = Integer.parseInt(String.valueOf(req.get("horario_termino")));
                at.setHorarioFim(LocalTime.of(h / 100, h % 100));
            }
            
            // Validação de cronologia da própria atividade
            if (at.getDataInicio() != null && at.getDataFim() != null && at.getHorarioInicio() != null && at.getHorarioFim() != null) {
                java.time.LocalDateTime datetimeIn = java.time.LocalDateTime.of(at.getDataInicio(), at.getHorarioInicio());
                java.time.LocalDateTime datetimeFi = java.time.LocalDateTime.of(at.getDataFim(), at.getHorarioFim());
                
                if (datetimeIn.isAfter(datetimeFi)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "A data e hora de início da atividade não podem ser posteriores ao término."));
                }
            }
            
            // Resolução Automática de Conflitos para Inscritos
            if (at.getDataInicio() != null && at.getHorarioInicio() != null && at.getDataFim() != null && at.getHorarioFim() != null) {
                java.util.List<br.unesp.fct.evcomp.domain.Inscrição> inscricoesParaVerificar = new java.util.ArrayList<>(at.getInscricoes());
                for (br.unesp.fct.evcomp.domain.Inscrição inscricao : inscricoesParaVerificar) {
                    if (inscricao.isStatus()) {
                        boolean conflitoEncontrado = false;
                        for (br.unesp.fct.evcomp.domain.Atividade outra : inscricao.getAtividade()) {
                            if (!outra.getId().equals(at.getId()) && at.verificarConflitoHorarios(outra)) {
                                conflitoEncontrado = true;
                                break;
                            }
                        }
                        if (conflitoEncontrado) {
                            inscricao.getAtividade().remove(at);
                            at.getInscricoes().remove(inscricao);
                            if (inscricao.getAtividade().isEmpty()) {
                                inscricao.setStatus(false);
                            }
                            inscricaoRepository.save(inscricao);
                            desinscritos++;
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Erro edicaoAtividade: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ocorreu um erro interno no servidor ao editar a atividade."));
        }
        
        atividadeRepository.save(at);
        
        if (desinscritos > 0) {
            return ResponseEntity.ok(Map.of("message", "Atividade editada com sucesso! ATENÇÃO: " + desinscritos + " participante(s) foram desinscritos automaticamente devido a conflito de horário."));
        }
        return ResponseEntity.ok(Map.of("message", "Atividade editada com sucesso!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirAtividade(@PathVariable Integer id, @RequestParam(required = false) boolean confirmar) {
        int inscritos = inscricaoRepository.contarInscritosPorAtividadeInt(id);
        if (inscritos > 0 && !confirmar) {
            return ResponseEntity.status(409).body(Map.of("error", "Atividade com participantes inscritos. Confirmar exclusão?"));
        }
        
        atividadeRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Atividade excluída com sucesso."));
    }

    @GetMapping("/{id}/vagas")
    public ResponseEntity<?> verificarVagas(@PathVariable("id") Integer atividadeId) {
        Integer vagas = atividadeRepository.consultarVagasDisponiveis(atividadeId);
        
        int vagasDisponiveis = vagas != null ? vagas : 0;

        return ResponseEntity.ok(Map.of("vagasDisponiveis", Math.max(0, vagasDisponiveis)));
    }
    @PostMapping("/verificar-conflitos")
    public ResponseEntity<?> verificarConflitosWeb(@RequestBody Map<String, Object> req) {
        List<Integer> atividades = (List<Integer>) req.get("atividades");
        Integer atividadeId = Integer.valueOf(String.valueOf(req.get("atividadeId")));
        return verificarConflitos(atividades, atividadeId);
    }

    public ResponseEntity<?> verificarConflitos(List<Integer> atividadesIds, Integer atividadeId) {
        Optional<Atividade> atvOpt = atividadeRepository.findById(atividadeId);
        if (!atvOpt.isPresent()) return ResponseEntity.badRequest().body(Map.of("error", "Atividade principal não encontrada."));
        
        Atividade atvPrincipal = atvOpt.get();
        for (Integer id : atividadesIds) {
            Optional<Atividade> outraOpt = atividadeRepository.findById(id);
            if (outraOpt.isPresent() && !id.equals(atividadeId)) {
                if (atvPrincipal.verificarConflitoHorarios(outraOpt.get())) {
                    return ResponseEntity.ok(Map.of("conflitoDetectado", true, "mensagem", "Conflito de horários com a atividade: " + outraOpt.get().getTitulo()));
                }
            }
        }
        return ResponseEntity.ok(Map.of("conflitoDetectado", false));
    }
}
