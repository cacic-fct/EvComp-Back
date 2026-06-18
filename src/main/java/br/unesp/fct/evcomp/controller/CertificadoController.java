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
    public ResponseEntity<?> solicitarDadosCertificados(@PathVariable Integer participanteId) {
        List<br.unesp.fct.evcomp.domain.Inscrição> inscricoes = inscricaoRepository.buscarInscricoesAtivasPorParticipante(participanteId);
        List<Atividade> atividadesMinistradas = atividadeRepository.buscarAtividadesPorMinistrante(participanteId);
        
        Map<Integer, Map<String, Object>> eventosAgrupados = new HashMap<>();

        for (Atividade atividade : atividadesMinistradas) {
            Evento evento = atividade.getEvento();
            Integer evId = evento.getId();
            
            if (!eventosAgrupados.containsKey(evId)) {
                Map<String, Object> evData = new HashMap<>();
                evData.put("eventoId", evId);
                evData.put("eventoTitulo", evento.getTitulo());
                evData.put("certificados", new ArrayList<Map<String, Object>>());
                eventosAgrupados.put(evId, evData);
            }

            Map<String, Object> map = new HashMap<>();
            map.put("tipo", "ATIVIDADE");
            map.put("id", atividade.getId());
            map.put("titulo", atividade.getTitulo() + " [Ministrante]");
            map.put("cargaHoraria", atividade.getCargaHorariaMinistrante());
            
            ((List<Map<String, Object>>) eventosAgrupados.get(evId).get("certificados")).add(map);
        }

        for (br.unesp.fct.evcomp.domain.Inscrição inscricao : inscricoes) {
            Evento evento = inscricao.getEvento();
            Integer evId = evento.getId();
            List<Atividade> atividades = inscricao.getAtividade();

            if (!eventosAgrupados.containsKey(evId)) {
                Map<String, Object> evData = new HashMap<>();
                evData.put("eventoId", evId);
                evData.put("eventoTitulo", evento.getTitulo());
                evData.put("certificados", new ArrayList<Map<String, Object>>());
                eventosAgrupados.put(evId, evData);
            }
            
            List<Map<String, Object>> certs = (List<Map<String, Object>>) eventosAgrupados.get(evId).get("certificados");

            if (evento.getTipoContabilizacao() != null && evento.getTipoContabilizacao().name().equals("POR_ATIVIDADE")) {
                for (Atividade atividade : atividades) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("tipo", "ATIVIDADE");
                    map.put("id", atividade.getId());
                    map.put("titulo", atividade.getTitulo());
                    map.put("cargaHoraria", atividade.getCargaHorariaTotal());
                    certs.add(map);
                }
            }
        }

        return ResponseEntity.ok(new ArrayList<>(eventosAgrupados.values()));
    }

    @PostMapping("/selecionar")
    public ResponseEntity<?> selecionarEventoOuAtividade(@RequestBody Map<String, Object> payload) {
        try {
            Integer participanteId = Integer.valueOf(payload.get("participanteId").toString());
            String tipo = payload.get("tipo").toString();
            Integer alvoId = Integer.valueOf(payload.get("alvoId").toString());

            Integer eventoId = "EVENTO".equals(tipo) ? alvoId : null;
            Integer atividadeId = "ATIVIDADE".equals(tipo) ? alvoId : null;

            if (eventoId != null) {
                boolean eventoEmAndamento = eventoRepository.checarAndamentoEvento(eventoId);

                if (eventoEmAndamento) {
                    return ResponseEntity.ok(Map.of("liberado", false, "motivo", "O evento ainda não foi finalizado."));
                }
                
                boolean presencaEvento = certificadoService.verificarPresencaPorEvento(participanteId, eventoId);
               
                int totalPresencas = presencaRepository.contarPresencasNoEvento(participanteId, eventoId);
                
                if (!presencaEvento || totalPresencas < 1) {
                    return ResponseEntity.ok(Map.of("liberado", false, "motivo", "Presença mínima não atingida neste evento."));
                }

            } else if (atividadeId != null) {
                
                boolean atividadeEmAndamento = atividadeRepository.checarAndamentoAtividade(atividadeId);
                
                Atividade atividade = atividadeRepository.buscarAtividadePorId(atividadeId).orElse(null);
                
                boolean eventoEmAndamento = atividade != null && eventoRepository.checarAndamentoEvento(atividade.getEvento().getId());
                
                if (atividadeEmAndamento || eventoEmAndamento) {
                    return ResponseEntity.ok(Map.of("liberado", false, "motivo", "A atividade (ou evento) ainda não foi finalizada."));
                }
                
                boolean presencaAtividade = certificadoService.verificarPresencaPorAtividade(participanteId, atividadeId);
              
                boolean isMinistrante = atividade != null && atividade.getMinistrantes().stream().anyMatch(m -> m.getId().equals(participanteId));
                
                if (!presencaAtividade && !isMinistrante) {
                    return ResponseEntity.ok(Map.of("liberado", false, "motivo", "Você não possui presença registrada nesta atividade."));
                }
            }
            
            return ResponseEntity.ok(Map.of("liberado", true, "motivo", "Liberado"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Erro ao selecionar certificado."));
        }
    }

    @PostMapping("/emitir")
    public ResponseEntity<?> emitirCertificado(@RequestBody Map<String, Object> payload) {
        try {
            Integer participanteId = Integer.valueOf(payload.get("participanteId").toString());
            String tipo = payload.get("tipo").toString(); // "EVENTO" ou "ATIVIDADE"
            Integer alvoId = Integer.valueOf(payload.get("alvoId").toString());

            Participante dadosParticipante = participanteRepository.buscarParticipantePorId(participanteId)
                .orElseThrow(() -> new RuntimeException("Participante não encontrado"));

            byte[] pdfBytes = null;
            String nomeArquivo = "";

            Integer eventoId = "EVENTO".equals(tipo) ? alvoId : null;
            Integer atividadeId = "ATIVIDADE".equals(tipo) ? alvoId : null;

            Map<String, Object> dadosEmissao = certificadoService.processarRegrasEmissao(dadosParticipante, eventoId, atividadeId);

            if (dadosEmissao.containsKey("error")) {
                return exibirMensagemErro((String) dadosEmissao.get("error"), 400);
            }

            Evento evento = (Evento) dadosEmissao.get("evento");
            Atividade atividade = atividadeId != null ? (Atividade) dadosEmissao.get("atividade") : null;



            pdfBytes = certificadoService.gerarCertificado(dadosParticipante, evento, atividade);
            nomeArquivo = dadosParticipante.getNome() + " - " + (atividade != null ? atividade.getTitulo() : evento.getTitulo()) + ".pdf";

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
