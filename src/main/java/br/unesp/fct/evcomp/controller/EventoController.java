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

    @Autowired
    public EventoController(EventoRepository eventoRepository, ParticipanteRepository participanteRepository) {
        this.eventoRepository = eventoRepository;
        this.participanteRepository = participanteRepository;
    }

    @GetMapping
    public ResponseEntity<List<Evento>> listarEventos() {
        return ResponseEntity.ok(eventoRepository.findAll());
    }

    public void buscarEvento(String eventoId) {}
    public void solicitarEventosDisponiveis(String participanteId) {}
    @PostMapping("/{eventoId}/coletores/{coletorId}")
    public ResponseEntity<?> associarColetorWeb(@PathVariable String eventoId, @PathVariable String coletorId) {
        try {
            Integer evId = Integer.valueOf(eventoId);
            Integer colId = Integer.valueOf(coletorId);
            participanteRepository.tornarColetorNoBanco(colId);
            participanteRepository.associarColetorAoEventoNoBanco(colId, evId);
            return ResponseEntity.ok().body(Map.of("message", "Coletor associado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao associar coletor: " + e.getMessage()));
        }
    }

    public void selecionarEvento(String eventoId) {}
    public void solicitarEventos() {}
    public void buscarParticipantesPorEvento(String eventoId) {}
    public void tornarColetor(String participanteId) {
        participanteRepository.tornarColetorNoBanco(Integer.valueOf(participanteId));
    }
    public void removerColetor(String eventoId, String coletorId) {}
    public void buscarColetoresPorEvento(String eventoId) {}
    public void solicitarDadosCertificados(String participanteId) {}
    public void confirmarCriacao(String titulo, java.util.Date dataInicio, java.util.Date dataTermino, String descricao, String link) {}
    public void confirmarConsulta(String tituloEvento) {}
    public void confirmarEdicao(String titulo, java.util.Date dataInicio, java.util.Date dataTermino, String descricao, String link) {}
}
