package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.domain.Evento;
import br.unesp.fct.evcomp.domain.Relatorio;
import br.unesp.fct.evcomp.repository.EventoRepository;
import br.unesp.fct.evcomp.repository.RelatorioRepository;
import br.unesp.fct.evcomp.service.relatorio.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/relatorios")

public class RelatorioController {

    private final RelatorioService relatorioService;
    private final RelatorioRepository relatorioRepository;
    private final EventoRepository eventoRepository;

    @Autowired
    public RelatorioController(RelatorioService relatorioService, RelatorioRepository relatorioRepository, EventoRepository eventoRepository) {
        this.relatorioService = relatorioService;
        this.relatorioRepository = relatorioRepository;
        this.eventoRepository = eventoRepository;
    }

    @GetMapping("/eventos")
    public ResponseEntity<?> listarEventosFinalizados() {
        List<Evento> eventos = eventoRepository.findAll().stream()
            .filter(this::isEventoFinalizado)
            .collect(Collectors.toList());
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/tipos")
    public ResponseEntity<?> listarTiposRelatorio() {
        return ResponseEntity.ok(relatorioService.obterTiposRelatoriosDisponiveis());
    }

    @PostMapping("/emitir")
    public ResponseEntity<?> emitirRelatorio(@RequestBody Map<String, Object> payload) {
        String eventoId = String.valueOf(payload.get("eventoId"));
        String tipo = String.valueOf(payload.get("tipo"));

        br.unesp.fct.evcomp.domain.TipoRelatorio tr = br.unesp.fct.evcomp.domain.TipoRelatorio.valueOf(tipo.toUpperCase());
        
        Optional<Evento> ev = eventoRepository.findById(Integer.valueOf(eventoId));
        if (ev.isPresent()) {
            Evento evento = ev.get();
            if (!isEventoFinalizado(evento)) {
                return ResponseEntity.badRequest().body(Map.of("error", "O evento ainda não foi finalizado."));
            }

            Relatorio rel = relatorioService.gerarRelatorio(evento, tr);
            relatorioRepository.save(rel);
            
            String filename = "Relatorio.pdf";
            if ("PARTICIPANTES".equals(tr.name())) {
                filename = "Relatorio Participantes - " + evento.getTitulo() + ".pdf";
            } else if ("GRAFICO".equals(tr.name())) {
                filename = "Relatorio Grafico - " + evento.getTitulo() + ".pdf";
            }

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(rel.getPdfConteudo());
        }
        return ResponseEntity.notFound().build();
    }

    private boolean isEventoFinalizado(Evento evento) {
        if (evento.getDataFim() == null) {
            return false;
        }
        if (evento.getDataInicio() != null && evento.getDataInicio().equals(evento.getDataFim())) {
            return LocalDate.now().isAfter(evento.getDataFim());
        } else {
            return !evento.getDataFim().isAfter(LocalDate.now());
        }
    }
}
