package br.unesp.fct.seccomp.controller;

import br.unesp.fct.seccomp.domain.Evento;
import br.unesp.fct.seccomp.domain.Relatorio;
import br.unesp.fct.seccomp.repository.EventoRepository;
import br.unesp.fct.seccomp.repository.RelatorioRepository;
import br.unesp.fct.seccomp.service.relatorio.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/relatorios")
@CrossOrigin(origins = "*")
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

    @GetMapping("/gerar")
    public ResponseEntity<?> gerarRelatorioWeb(@RequestParam String eventoId, @RequestParam String tipo) {
        selecionarEvento(eventoId);
        br.unesp.fct.seccomp.domain.TipoRelatorio tr = br.unesp.fct.seccomp.domain.TipoRelatorio.valueOf(tipo.toUpperCase());
        
        Optional<Evento> ev = eventoRepository.findById(Long.valueOf(eventoId));
        if (ev.isPresent()) {
            gerarRelatorio(ev.get(), tr);
            return ResponseEntity.ok().body(Map.of("message", "Relatorio " + tipo + " gerado"));
        }
        return ResponseEntity.notFound().build();
    }

    public void gerarRelatorio(Object dadosEvento, br.unesp.fct.seccomp.domain.TipoRelatorio tipoRelatorio) {
        if (dadosEvento instanceof Evento) {
            Relatorio rel = relatorioService.gerarRelatorio((Evento)dadosEvento, tipoRelatorio);
            relatorioRepository.save(rel);
        }
    }
    public void selecionarEvento(String eventoId) {}
}
