package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.domain.Evento;
import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.domain.TipoContabilizacao;
import br.unesp.fct.evcomp.repository.EventoRepository;
import br.unesp.fct.evcomp.repository.ParticipanteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/eventos")

public class EventoController {

    private final EventoRepository eventoRepository;
    private final ParticipanteRepository participanteRepository;
    private final br.unesp.fct.evcomp.repository.SessaoRepository sessaoRepository;
    private final br.unesp.fct.evcomp.repository.InscricaoRepository inscricaoRepository;
    
    @Autowired
    private br.unesp.fct.evcomp.repository.AtividadeRepository atividadeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public EventoController(EventoRepository eventoRepository, ParticipanteRepository participanteRepository, br.unesp.fct.evcomp.repository.SessaoRepository sessaoRepository, br.unesp.fct.evcomp.repository.InscricaoRepository inscricaoRepository) {
        this.eventoRepository = eventoRepository;
        this.participanteRepository = participanteRepository;
        this.sessaoRepository = sessaoRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @GetMapping
    public ResponseEntity<List<Evento>> listarEventos() {
        return ResponseEntity.ok(eventoRepository.findAll());
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<Evento>> listarEventosDisponiveis() {
        return ResponseEntity.ok(eventoRepository.buscarEventosDisponiveis());
    }

    @GetMapping("/disponiveis/{participanteId}")
    public ResponseEntity<List<Evento>> solicitarEventosDisponiveis(@PathVariable Integer participanteId) {
        return ResponseEntity.ok(eventoRepository.buscarEventosDisponiveisPorParticipante(participanteId));
    }

    @GetMapping("/{id}/participantes")
    public ResponseEntity<List<Participante>> listarParticipantesDoEvento(@PathVariable Integer id) {
        // Conforme Diagrama de Colaboração: buscarParticipantesPorEvento
        List<Participante> participantes = inscricaoRepository.buscarParticipantesPorEvento(id);
        return ResponseEntity.ok(participantes);
    }

    @GetMapping("/coletor")
    public ResponseEntity<?> listarEventosDoColetor(jakarta.servlet.http.HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("auth_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Token não fornecido ou inválido."));
        }
        
        Optional<br.unesp.fct.evcomp.domain.Sessao> sessaoOpt = sessaoRepository.buscarSessaoPorToken(token);
        
        if (sessaoOpt.isPresent() && sessaoOpt.get().isAtiva()) {
            br.unesp.fct.evcomp.domain.Usuário userSessao = sessaoOpt.get().getUsuario();
            
            // Buscar o usuário fresquinho do banco para garantir que as coleções carreguem certo
            Optional<Participante> partOpt = participanteRepository.buscarParticipantePorId(userSessao.getId());
            
            if (partOpt.isPresent() && partOpt.get() instanceof br.unesp.fct.evcomp.domain.ColetorDePresenca) {
                br.unesp.fct.evcomp.domain.ColetorDePresenca coletor = (br.unesp.fct.evcomp.domain.ColetorDePresenca) partOpt.get();
                List<Evento> eventosColetados = coletor.getEventosColetados();
                
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

    @PostMapping("/{eventoId}/coletores/{participanteId}")
    public ResponseEntity<?> tornarColetor(@PathVariable String eventoId, @PathVariable("participanteId") String participanteId) {
        try {
            Integer evId = Integer.valueOf(eventoId);
            Optional<Participante> partOpt = participanteRepository.buscarPorRa(participanteId);
            if (!partOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Participante não encontrado com este RA."));
            }
            Participante p = partOpt.get();
            Integer usuId = p.getId();
            
            // Mutação Nativa: Define o participante como coletor no banco (muda tipo_usuario)
            participanteRepository.tornarColetor(usuId);
            // Limpa o cache para obrigar o JPA a reler o objeto como ColetorDePresenca
            entityManager.clear();
            
            // Insere na tabela N:M
            participanteRepository.associarColetorAoEventoNoBanco(usuId, evId);
            
            return ResponseEntity.ok(Map.of("message", "Coletor associado com sucesso."));
        } catch (Exception e) {
            System.err.println("Erro ao associar coletor: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ocorreu um erro interno no servidor ao associar o coletor."));
        }
    }

    @DeleteMapping("/{eventoId}/coletores/{coletorId}")
    public ResponseEntity<?> removerColetor(@PathVariable String eventoId, @PathVariable String coletorId) {
        try {
            Integer evId = Integer.valueOf(eventoId);
            Integer usuId = Integer.valueOf(coletorId);
            
            // Remove do banco a associação na tabela N:M
            participanteRepository.removerColetorDoEventoNoBanco(usuId, evId);
            
            // Verifica se ficou sem eventos
            int count = participanteRepository.contarEventosDoColetor(usuId);
            if (count == 0) {
                // Downgrade: Volta a ser Participante comum
                participanteRepository.rebaixarParaParticipante(usuId);
            }
            
            entityManager.clear();
            
            return ResponseEntity.ok(Map.of("message", "Coletor removido com sucesso."));
        } catch (Exception e) {
            System.err.println("Erro ao remover coletor: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ocorreu um erro interno ao remover o coletor."));
        }
    }

    @GetMapping("/{id}/detalhes-inscricao")
    public ResponseEntity<?> selecionarEvento(@PathVariable Integer eventoId) {
        Optional<Evento> eventoOpt = eventoRepository.buscarEventoPorId(eventoId);

        if (!eventoOpt.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("error", "Evento não encontrado."));
        }

        Evento evento = eventoOpt.get();

        Map<String, Object> dadosEvento = evento.pegarDadosEvento();

        List<br.unesp.fct.evcomp.domain.Atividade> atividades = atividadeRepository.buscarAtividadesPorEvento(eventoId);
        
        return ResponseEntity.ok(Map.of(
            "dadosEvento", dadosEvento,
            "atividades", atividades
        ));
    }

    public void solicitarEventos() {}
    public void buscarParticipantesPorEvento(String eventoId) {}
    public void buscarColetoresPorEvento(String eventoId) {}
    public void solicitarDadosCertificados(String participanteId) {}
    @PostMapping
    public ResponseEntity<?> criarEventoWeb(@RequestBody Map<String, String> req) {
        try {
            LocalDate dataInicio = null;
            LocalDate dataTermino = null;
            if (req.get("dataInicio") != null && !req.get("dataInicio").isEmpty()) {
                dataInicio = LocalDate.parse(req.get("dataInicio"));
            }
            if (req.get("dataTermino") != null && !req.get("dataTermino").isEmpty()) {
                dataTermino = LocalDate.parse(req.get("dataTermino"));
            }
            return confirmarCriacao(req.get("titulo"), dataInicio, dataTermino, req.get("descricao"), req.get("link"), req.get("tipoContabilizacao"));
        } catch (Exception e) {
            System.err.println("Erro ao criar evento: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ocorreu um erro interno no servidor ao cadastrar o evento."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarEventoWeb(@PathVariable Integer id, @RequestBody Map<String, String> req) {
        try {
            LocalDate dataInicio = null;
            LocalDate dataTermino = null;
            if (req.get("dataInicio") != null && !req.get("dataInicio").isEmpty()) {
                dataInicio = LocalDate.parse(req.get("dataInicio"));
            }
            if (req.get("dataTermino") != null && !req.get("dataTermino").isEmpty()) {
                dataTermino = LocalDate.parse(req.get("dataTermino"));
            }
            return confirmarEdicao(id, req.get("titulo"), dataInicio, dataTermino, req.get("descricao"), req.get("link"), req.get("tipoContabilizacao"));
        } catch (Exception e) {
            System.err.println("Erro ao editar evento: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ocorreu um erro interno no servidor ao editar o evento."));
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> consultarEventoWeb(@RequestParam String titulo) {
        return confirmarConsulta(titulo);
    }

    public ResponseEntity<?> confirmarCriacao(String titulo, LocalDate dataInicio, LocalDate dataTermino, String descricao, String link, String tipo) {
        if (dataInicio == null || dataTermino == null || titulo == null || descricao == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Campos obrigatórios ausentes."));
        }
        if (dataTermino.isBefore(dataInicio)) {
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
        java.util.List<Evento> evs = eventoRepository.buscarEventosPorTituloParcial(tituloEvento);
        return evs.isEmpty() ? ResponseEntity.status(404).body(Map.of("error", "Nenhum evento encontrado com este título.")) : ResponseEntity.ok(evs);
    }

    public ResponseEntity<?> confirmarEdicao(Integer id, String titulo, LocalDate dataInicio, LocalDate dataTermino, String descricao, String link, String tipo) {
        Optional<Evento> evOpt = eventoRepository.buscarEventoPorId(id);
        if (!evOpt.isPresent()) return ResponseEntity.status(404).body(Map.of("error", "Evento não encontrado."));
        
        Evento evento = evOpt.get();
        if (dataTermino != null && dataInicio != null && dataTermino.isBefore(dataInicio)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Data de término não pode ser anterior à data de início."));
        }
        if (titulo != null && !titulo.equals(evento.getTitulo()) && eventoRepository.verificarEventoCadastrado(titulo)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Já existe um evento cadastrado com este título."));
        }
        
        if (titulo != null) evento.setTitulo(titulo);
        if (dataInicio != null) {
            evento.setDataInicio(dataInicio);
        }
        if (dataTermino != null) {
            evento.setDataFim(dataTermino);
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
