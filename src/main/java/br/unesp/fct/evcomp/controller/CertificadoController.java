package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.domain.Atividade;
import br.unesp.fct.evcomp.domain.Certificado;
import br.unesp.fct.evcomp.domain.Evento;
import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.repository.AtividadeRepository;
import br.unesp.fct.evcomp.repository.CertificadoRepository;
import br.unesp.fct.evcomp.repository.EventoRepository;
import br.unesp.fct.evcomp.repository.InscricaoRepository;
import br.unesp.fct.evcomp.repository.ParticipanteRepository;
import br.unesp.fct.evcomp.repository.PresencaRepository;
import br.unesp.fct.evcomp.service.certificado.CertificadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/certificados")
@CrossOrigin(origins = "*")
public class CertificadoController {

    private final CertificadoService certificadoService;
    private final CertificadoRepository certificadoRepository;
    private final ParticipanteRepository participanteRepository;
    private final EventoRepository eventoRepository;
    private final AtividadeRepository atividadeRepository;
    private final InscricaoRepository inscricaoRepository;
    
    @Autowired
    private PresencaRepository presencaRepository;

    @Autowired
    public CertificadoController(CertificadoService certificadoService, CertificadoRepository certificadoRepository, ParticipanteRepository participanteRepository, EventoRepository eventoRepository, AtividadeRepository atividadeRepository, InscricaoRepository inscricaoRepository) {
        this.certificadoService = certificadoService;
        this.certificadoRepository = certificadoRepository;
        this.participanteRepository = participanteRepository;
        this.eventoRepository = eventoRepository;
        this.atividadeRepository = atividadeRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @GetMapping("/disponiveis/{participanteId}")
    public ResponseEntity<?> listarDisponiveis(@PathVariable Integer participanteId) {
        List<br.unesp.fct.evcomp.domain.Inscrição> inscricoes = inscricaoRepository.buscarInscricoesAtivasPorParticipante(participanteId);
        List<Atividade> atividadesMinistradas = atividadeRepository.buscarAtividadesPorMinistrante(participanteId);
        List<Map<String, Object>> response = new ArrayList<>();

        for (Atividade atividade : atividadesMinistradas) {
            Evento evento = atividade.getEvento();
            Map<String, Object> map = new HashMap<>();
            map.put("tipo", "ATIVIDADE");
            map.put("id", atividade.getId());
            map.put("titulo", atividade.getTitulo() + " (" + evento.getTitulo() + ") [Ministrante]");
            map.put("cargaHoraria", atividade.getCargaHorariaMinistrante());

            boolean finalizado = isEventoFinalizado(evento);

            if (!finalizado) {
                map.put("liberado", false);
                map.put("motivo", "Em andamento");
            } else {
                map.put("liberado", true);
                map.put("motivo", "Liberado");
            }
            response.add(map);
        }

        for (br.unesp.fct.evcomp.domain.Inscrição inscricao : inscricoes) {
            Evento evento = inscricao.getEvento();
            List<Atividade> atividades = inscricao.getAtividade();

            if (evento.getTipoContabilizacao() != null && evento.getTipoContabilizacao().name().equals("POR_ATIVIDADE")) {
                for (Atividade atividade : atividades) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("tipo", "ATIVIDADE");
                    map.put("id", atividade.getId());
                    map.put("titulo", atividade.getTitulo() + " (" + evento.getTitulo() + ")");
                    map.put("cargaHoraria", atividade.getCargaHorariaTotal());

                    boolean presente = presencaRepository.buscarPresencaPorAtividade(atividade.getId(), participanteId).filter(p -> p.isPresente()).isPresent();
                    boolean finalizado = isEventoFinalizado(evento);

                    if (!presente) {
                        map.put("liberado", false);
                        map.put("motivo", "Falta de Presença");
                    } else if (!finalizado) {
                        map.put("liberado", false);
                        map.put("motivo", "Em andamento");
                    } else {
                        map.put("liberado", true);
                        map.put("motivo", "Liberado");
                    }
                    response.add(map);
                }
            } else {
                // POR_CARGA_TOTAL
                int totalAtividades = atividades.size();
                int presencas = 0;
                int cargaHorariaTotal = 0;

                for (Atividade a : atividades) {
                    if (presencaRepository.buscarPresencaPorAtividade(a.getId(), participanteId).filter(p -> p.isPresente()).isPresent()) {
                        presencas++;
                    }
                    cargaHorariaTotal += a.getCargaHorariaTotal();
                }

                Map<String, Object> mapEv = new HashMap<>();
                mapEv.put("tipo", "EVENTO");
                mapEv.put("id", evento.getId());
                mapEv.put("titulo", evento.getTitulo());

                double ratio = totalAtividades > 0 ? (double) presencas / totalAtividades : 0;
                boolean frequenciaSuficiente = totalAtividades == 0 || ratio >= 0.5;
                boolean finalizado = isEventoFinalizado(evento);

                mapEv.put("cargaHoraria", frequenciaSuficiente ? (ratio == 1.0 ? cargaHorariaTotal : cargaHorariaTotal / 2) : 0);

                if (!frequenciaSuficiente || presencas == 0) {
                    mapEv.put("liberado", false);
                    mapEv.put("motivo", presencas == 0 ? "Falta de Presença" : "Frequência Insuficiente");
                } else if (!finalizado) {
                    mapEv.put("liberado", false);
                    mapEv.put("motivo", "Em andamento");
                } else {
                    mapEv.put("liberado", true);
                    mapEv.put("motivo", "Liberado");
                }
                response.add(mapEv);
            }
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/emitir")
    public ResponseEntity<?> emitirCertificado(@RequestBody Map<String, Object> payload) {
        try {
            Integer participanteId = Integer.valueOf(payload.get("participanteId").toString());
            String tipo = payload.get("tipo").toString(); // "EVENTO" ou "ATIVIDADE"
            Integer alvoId = Integer.valueOf(payload.get("alvoId").toString());

            Participante participante = participanteRepository.findById(participanteId)
                .orElseThrow(() -> new RuntimeException("Participante não encontrado"));

            byte[] pdfBytes;
            String nomeArquivo = "";

            if ("EVENTO".equals(tipo)) {
                Evento evento = eventoRepository.findById(alvoId).orElseThrow(() -> new RuntimeException("Evento não encontrado"));
                if (!isEventoFinalizado(evento)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "O evento ainda não foi finalizado."));
                }
                
                long presencas = presencaRepository.contarPresencasNoEvento(participanteId, alvoId);
                if (presencas == 0) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Presença mínima não atingida neste evento."));
                }

                pdfBytes = certificadoService.emitirCertificado(participante, evento, null);
                nomeArquivo = participante.getNome() + " - " + evento.getTitulo() + ".pdf";

            } else {
                Atividade atividade = atividadeRepository.findById(alvoId).orElseThrow(() -> new RuntimeException("Atividade não encontrada"));
                Evento evento = atividade.getEvento();
                if (!isEventoFinalizado(evento)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "O evento ao qual a atividade pertence ainda não foi finalizado."));
                }

                boolean isMinistrante = atividade.getMinistrantes().stream().anyMatch(m -> m.getId().equals(participanteId));

                if (!isMinistrante) {
                    boolean estevePresente = presencaRepository.buscarPresencaPorAtividade(alvoId, participanteId)
                        .filter(p -> p.isPresente()).isPresent();
                    
                    if (!estevePresente) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Presença não registrada para esta atividade."));
                    }
                }

                pdfBytes = certificadoService.emitirCertificado(participante, evento, atividade);
                nomeArquivo = participante.getNome() + " - " + atividade.getTitulo() + ".pdf";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            
            org.springframework.http.ContentDisposition contentDisposition = org.springframework.http.ContentDisposition.attachment()
                    .filename(nomeArquivo.replaceAll("[\\\\/:*?\\\"<>|]", "_"))
                    .build();
            headers.setContentDisposition(contentDisposition);
            headers.add("Access-Control-Expose-Headers", "Content-Disposition");

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Ocorreu um erro interno ao processar a emissão do certificado. Tente novamente mais tarde."));
        }
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
