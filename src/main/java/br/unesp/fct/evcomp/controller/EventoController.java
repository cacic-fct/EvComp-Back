package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.domain.Evento;
import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.domain.TipoContabilizacao;
import br.unesp.fct.evcomp.repository.EventoRepository;
import br.unesp.fct.evcomp.repository.ParticipanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/eventos")
@CrossOrigin(origins = "*")
public class EventoController {

    private final EventoRepository eventoRepository;
    private final ParticipanteRepository participanteRepository;
    private final br.unesp.fct.evcomp.repository.SessaoRepository sessaoRepository;

    @Autowired
    public EventoController(EventoRepository eventoRepository, ParticipanteRepository participanteRepository, br.unesp.fct.evcomp.repository.SessaoRepository sessaoRepository) {
        this.eventoRepository = eventoRepository;
        this.participanteRepository = participanteRepository;
        this.sessaoRepository = sessaoRepository;
    }

    @GetMapping
    public ResponseEntity<List<Evento>> listarEventos() {
        return ResponseEntity.ok(eventoRepository.findAll());
    }

    @GetMapping("/coletor")
    public ResponseEntity<?> listarEventosDoColetor(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Token não fornecido ou inválido."));
        }
        String token = authHeader.substring(7);
        Optional<br.unesp.fct.evcomp.domain.Sessao> sessaoOpt = sessaoRepository.buscarSessaoPorToken(token);
        
        if (sessaoOpt.isPresent() && sessaoOpt.get().isAtiva()) {
            br.unesp.fct.evcomp.domain.Usuário user = sessaoOpt.get().getUsuario();
            if (user instanceof br.unesp.fct.evcomp.domain.ColetorDePresenca) {
                List<Evento> eventosColetados = ((br.unesp.fct.evcomp.domain.ColetorDePresenca) user).getEventosColetados();
                
                // Filtra eventos que estão ocorrendo no momento
                LocalDate dataAtual = LocalDate.now();
                List<Evento> eventosAtivos = eventosColetados.stream()
                        .filter(e -> (e.getDataInicio() == null || !dataAtual.isBefore(e.getDataInicio())) && 
                                     (e.getDataFim() == null || !dataAtual.isAfter(e.getDataFim())))
                        .toList();
                        
                return ResponseEntity.ok(eventosAtivos);
            } else {
                return ResponseEntity.status(403).body(Map.of("error", "Usuário não é um Coletor"));
            }
        }
        return ResponseEntity.status(401).body(Map.of("error", "Sessão inválida ou expirada."));
    }

    public void buscarEvento(String eventoId) {}
    public void solicitarEventosDisponiveis(String participanteId) {}

    @PostMapping("/{eventoId}/coletores/{coletorId}")
    public ResponseEntity<?> associarColetorWeb(@PathVariable String eventoId, @PathVariable String coletorId) {
        try {
            Integer evId = Integer.valueOf(eventoId);
            Integer usuId = Integer.valueOf(coletorId);
            
            // Define o participante como coletor no banco (muda tipo_usuario)
            participanteRepository.tornarColetor(usuId);
            // Insere na tabela N:M
            participanteRepository.associarColetorAoEventoNoBanco(usuId, evId);
            
            return ResponseEntity.ok(Map.of("message", "Coletor associado com sucesso."));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", "Erro ao associar coletor: " + e.getMessage()));
        }
    }

    public void selecionarEvento(String eventoId) {}
    public void solicitarEventos() {}
    public void buscarParticipantesPorEvento(String eventoId) {}
    public void tornarColetor(String participanteId, String eventoId) {
        participanteRepository.tornarColetor(Integer.valueOf(participanteId));
        participanteRepository.associarColetorAoEventoNoBanco(Integer.valueOf(participanteId), Integer.valueOf(eventoId));
    }
    public void removerColetor(String eventoId, String coletorId) {}
    public void buscarColetoresPorEvento(String eventoId) {}
    public void solicitarDadosCertificados(String participanteId) {}
    @PostMapping
    public ResponseEntity<?> criarEventoWeb(@RequestBody Map<String, String> req) {
        try {
            java.util.Date dataInicio = null;
            java.util.Date dataTermino = null;
            if (req.get("dataInicio") != null && !req.get("dataInicio").isEmpty()) {
                dataInicio = java.util.Date.from(LocalDate.parse(req.get("dataInicio")).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            }
            if (req.get("dataTermino") != null && !req.get("dataTermino").isEmpty()) {
                dataTermino = java.util.Date.from(LocalDate.parse(req.get("dataTermino")).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            }
            return confirmarCriacao(req.get("titulo"), dataInicio, dataTermino, req.get("descricao"), req.get("link"), req.get("tipoContabilizacao"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Erro ao processar os dados: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarEventoWeb(@PathVariable Integer id, @RequestBody Map<String, String> req) {
        try {
            java.util.Date dataInicio = null;
            java.util.Date dataTermino = null;
            if (req.get("dataInicio") != null && !req.get("dataInicio").isEmpty()) {
                dataInicio = java.util.Date.from(LocalDate.parse(req.get("dataInicio")).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            }
            if (req.get("dataTermino") != null && !req.get("dataTermino").isEmpty()) {
                dataTermino = java.util.Date.from(LocalDate.parse(req.get("dataTermino")).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            }
            return confirmarEdicao(id, req.get("titulo"), dataInicio, dataTermino, req.get("descricao"), req.get("link"), req.get("tipoContabilizacao"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Erro ao processar os dados: " + e.getMessage()));
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> consultarEventoWeb(@RequestParam String titulo) {
        return confirmarConsulta(titulo);
    }

    public ResponseEntity<?> confirmarCriacao(String titulo, java.util.Date dataInicio, java.util.Date dataTermino, String descricao, String link, String tipo) {
        if (dataInicio == null || dataTermino == null || titulo == null || descricao == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Campos obrigatórios ausentes."));
        }
        if (dataTermino.before(dataInicio)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Data de término não pode ser anterior à data de início."));
        }
        if (eventoRepository.verificarEventoCadastrado(titulo)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Já existe um evento cadastrado com este título."));
        }
        TipoContabilizacao tipoC = TipoContabilizacao.POR_ATIVIDADE;
        if (tipo != null) {
            try { tipoC = TipoContabilizacao.valueOf(tipo); } catch(Exception e) {}
        }
        Evento evento = new Evento(titulo, dataInicio, dataTermino, descricao, link, tipoC);
        eventoRepository.save(evento);
        return ResponseEntity.ok(evento);
    }

    public ResponseEntity<?> confirmarConsulta(String tituloEvento) {
        Optional<Evento> ev = eventoRepository.buscarEventoPorTitulo(tituloEvento);
        return ev.isPresent() ? ResponseEntity.ok(ev.get()) : ResponseEntity.status(404).body(Map.of("error", "Evento não encontrado."));
    }

    public ResponseEntity<?> confirmarEdicao(Integer id, String titulo, java.util.Date dataInicio, java.util.Date dataTermino, String descricao, String link, String tipo) {
        Optional<Evento> evOpt = eventoRepository.buscarEventoPorIdInt(id);
        if (!evOpt.isPresent()) return ResponseEntity.status(404).body(Map.of("error", "Evento não encontrado."));
        
        Evento evento = evOpt.get();
        if (dataTermino != null && dataInicio != null && dataTermino.before(dataInicio)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Data de término não pode ser anterior à data de início."));
        }
        if (titulo != null && !titulo.equals(evento.getTitulo()) && eventoRepository.verificarEventoCadastrado(titulo)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Já existe um evento cadastrado com este título."));
        }
        
        if (titulo != null) evento.setTitulo(titulo);
        // Fix the date setting! Evento uses LocalDate in its setters, so we need to convert back or bypass it.
        // But since we parsed it, we can just use the LocalDate setters
        if (dataInicio != null) {
            LocalDate ldInicio = dataInicio.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            evento.setDataInicio(ldInicio);
        }
        if (dataTermino != null) {
            LocalDate ldTermino = dataTermino.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            evento.setDataFim(ldTermino);
        }
        if (descricao != null) evento.setDescricao(descricao);
        if (link != null) evento.setLink(link);
        if (tipo != null) {
            try { evento.setTipoContabilizacao(TipoContabilizacao.valueOf(tipo)); } catch(Exception e) {}
        }
        
        eventoRepository.save(evento);
        return ResponseEntity.ok(evento);
    }
}
