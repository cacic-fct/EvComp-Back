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
    private br.unesp.fct.evcomp.service.CalculadoraCargaHoraria calculadoraCargaHoraria;

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
    public ResponseEntity<?> selecionarEventoOuAtividade(@PathVariable Integer participanteId) {
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

            boolean andamento = eventoRepository.checarAndamentoEvento(String.valueOf(evento.getId()));

            if (andamento) {
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

                    boolean presente = certificadoService.verificarPresencaPorAtividade(participanteId.toString(), String.valueOf(atividade.getId()));
                    boolean andamento = eventoRepository.checarAndamentoEvento(String.valueOf(evento.getId()));

                    if (!presente) {
                        map.put("liberado", false);
                        map.put("motivo", "Falta de Presença");
                    } else if (andamento) {
                        map.put("liberado", false);
                        map.put("motivo", "Em andamento");
                    } else {
                        map.put("liberado", true);
                        map.put("motivo", "Liberado");
                    }
                    response.add(map);
                }
            }
            
            // Certificado GERAL do evento é gerado sempre, inclusive para "POR_ATIVIDADE"
            int totalAtividades = atividades.size();
            int presencas = presencaRepository.contarPresencasNoEvento(participanteId.toString(), String.valueOf(evento.getId()));
            int cargaHorariaTotal = calculadoraCargaHoraria.calcularCargaHorariaTotal(evento);

            Map<String, Object> mapEv = new HashMap<>();
            mapEv.put("tipo", "EVENTO");
            mapEv.put("id", evento.getId());
            mapEv.put("titulo", evento.getTitulo());

            boolean frequenciaSuficiente = certificadoService.verificarPresencaPorEvento(participanteId.toString(), String.valueOf(evento.getId()));
            boolean andamento = eventoRepository.checarAndamentoEvento(String.valueOf(evento.getId()));
            double ratio = totalAtividades > 0 ? (double) presencas / totalAtividades : 0;

            mapEv.put("cargaHoraria", frequenciaSuficiente ? (ratio == 1.0 ? cargaHorariaTotal : cargaHorariaTotal / 2) : 0);

            if (!frequenciaSuficiente || presencas == 0) {
                mapEv.put("liberado", false);
                mapEv.put("motivo", presencas == 0 ? "Falta de Presença" : "Frequência Insuficiente");
            } else if (andamento) {
                mapEv.put("liberado", false);
                mapEv.put("motivo", "Em andamento");
            } else {
                mapEv.put("liberado", true);
                mapEv.put("motivo", "Liberado");
            }
            response.add(mapEv);
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

            byte[] pdfBytes = null;
            String nomeArquivo = "";

            Integer eventoId = "EVENTO".equals(tipo) ? alvoId : null;
            Integer atividadeId = "ATIVIDADE".equals(tipo) ? alvoId : null;

            Map<String, Object> dadosEmissao = certificadoService.processarRegrasEmissao(participante, eventoId, atividadeId);

            if (dadosEmissao.containsKey("error")) {
                return exibirMensagemErro((String) dadosEmissao.get("error"), 400);
            }

            Evento evento = (Evento) dadosEmissao.get("evento");
            Atividade atividade = atividadeId != null ? (Atividade) dadosEmissao.get("atividade") : null;



            pdfBytes = certificadoService.gerarCertificado(participante, evento, atividade);
            nomeArquivo = participante.getNome() + " - " + (atividade != null ? atividade.getTitulo() : evento.getTitulo()) + ".pdf";

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

    public ResponseEntity<?> exibirMensagemErro(String msg, int status) {
        return ResponseEntity.status(status).body(Map.of("error", msg));
    }
}
