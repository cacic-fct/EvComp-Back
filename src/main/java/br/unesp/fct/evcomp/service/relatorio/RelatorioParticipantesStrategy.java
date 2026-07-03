package br.unesp.fct.evcomp.service.relatorio;


import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.domain.Relatorio;
import br.unesp.fct.evcomp.repository.InscricaoRepository;
import br.unesp.fct.evcomp.service.PDFGenerator;
import org.springframework.util.FileCopyUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import br.unesp.fct.evcomp.repository.AtividadeRepository;
import br.unesp.fct.evcomp.domain.Atividade;

@Component
public class RelatorioParticipantesStrategy extends RelatorioStrategyFactory {

    private final InscricaoRepository inscricaoRepository;
    private final AtividadeRepository atividadeRepository;
    private final PDFGenerator pdfGenerator;

    @Autowired
    public RelatorioParticipantesStrategy(InscricaoRepository inscricaoRepository, AtividadeRepository atividadeRepository, PDFGenerator pdfGenerator) {
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.pdfGenerator = pdfGenerator;
    }

    @Override
    public Object processarDados(Integer eventoId) {
        List<Atividade> atividades = atividadeRepository.buscarAtividadesPorEvento(eventoId);
        List<Map<String, Object>> blocos = new ArrayList<>();

        for (Atividade atv : atividades) {
            Map<String, Object> bloco = new HashMap<>();
            bloco.put("tituloAtividade", atv.getTitulo());
            
            List<Map<String, String>> pessoas = new ArrayList<>();

            // 1. Ministrantes
            for (br.unesp.fct.evcomp.domain.Usuário m : atv.getMinistrantes()) {
                pessoas.add(Map.of("nome", m.getNomeCompleto(), "email", m.getEmail(), "ra", "Ministrante"));
            }

            // 2. Inscritos Confirmados
            for (br.unesp.fct.evcomp.domain.Inscrição insc : atv.getInscricoes()) {
                if (insc.isStatus()) {
                    br.unesp.fct.evcomp.domain.Participante p = insc.getParticipante();
                    String raStr = (p.getRA() != null && !p.getRA().trim().isEmpty()) ? p.getRA() : "Externo";
                    pessoas.add(Map.of("nome", p.getNomeCompleto(), "email", p.getEmail(), "ra", raStr));
                }
            }
            
            bloco.put("pessoas", pessoas);
            blocos.add(bloco);
        }
        return blocos;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Relatorio gerarPDF(Object dadosBrutos, String tituloEvento) {
        List<Map<String, Object>> blocosAtividades = (List<Map<String, Object>>) dadosBrutos;
        
        try {
            InputStream in = getClass().getResourceAsStream("/templates/relatorio_participantes.html");
            if (in == null) {
                throw new RuntimeException("Template HTML não encontrado: relatorio_participantes.html");
            }
            byte[] bdata = FileCopyUtils.copyToByteArray(in);
            String html = new String(bdata, StandardCharsets.UTF_8);

            html = html.replace("$nomeEvento", tituloEvento);
            html = html.replace("$dataGeracao", LocalDateTime.now().toString());

            StringBuilder blocosHtml = new StringBuilder();
            for (Map<String, Object> bloco : blocosAtividades) {
                String tituloAtividade = (String) bloco.get("tituloAtividade");
                List<Map<String, String>> pessoas = (List<Map<String, String>>) bloco.get("pessoas");
                
                blocosHtml.append("<div class=\"atividade-container\">");
                blocosHtml.append("<h3>Atividade: ").append(tituloAtividade).append("</h3>");
                blocosHtml.append("<table>");
                blocosHtml.append("<thead><tr><th style=\"width: 40%\">Nome</th><th style=\"width: 40%\">E-mail</th><th style=\"width: 20%\">RA / Papel</th></tr></thead>");
                blocosHtml.append("<tbody>");
                
                if (pessoas.isEmpty()) {
                    blocosHtml.append("<tr><td colspan=\"3\" style=\"text-align:center\">Nenhum participante ou ministrante confirmado.</td></tr>");
                } else {
                    for (Map<String, String> p : pessoas) {
                        blocosHtml.append("<tr>");
                        blocosHtml.append("<td>").append(p.get("nome")).append("</td>");
                        blocosHtml.append("<td>").append(p.get("email")).append("</td>");
                        blocosHtml.append("<td>").append(p.get("ra")).append("</td>");
                        blocosHtml.append("</tr>");
                    }
                }
                
                blocosHtml.append("</tbody></table></div>");
            }
            
            html = html.replace("$blocosAtividades", blocosHtml.toString());

            byte[] pdfBytes = pdfGenerator.gerarPDF(html);
            return new Relatorio(new java.util.Date(), "PARTICIPANTES_ATIVIDADE", pdfBytes);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar PDF de relatório de participantes", e);
        }
    }
}
