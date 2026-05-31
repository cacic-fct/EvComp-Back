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
    public void confirmarCriacao(String titulo, java.util.Date dataInicio, java.util.Date dataTermino, String descricao, String link) {}
    public void confirmarConsulta(String tituloEvento) {}
    public void confirmarEdicao(String titulo, java.util.Date dataInicio, java.util.Date dataTermino, String descricao, String link) {}
}
