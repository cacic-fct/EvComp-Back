package br.unesp.fct.evcomp.service.relatorio;

import br.unesp.fct.evcomp.domain.Evento;
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

@Component
public class RelatorioParticipantesStrategy extends RelatorioStrategyFactory {

    private final InscricaoRepository inscricaoRepository;
    private final PDFGenerator pdfGenerator;

    @Autowired
    public RelatorioParticipantesStrategy(InscricaoRepository inscricaoRepository, PDFGenerator pdfGenerator) {
        this.inscricaoRepository = inscricaoRepository;
        this.pdfGenerator = pdfGenerator;
    }

    @Override
    public Object processarDados(Evento dadosEvento) {
        return inscricaoRepository.buscarParticipantesPorEvento(dadosEvento.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Relatorio gerarPDF(Object dadosBrutos, Evento evento) {
        List<Participante> participantes = (List<Participante>) dadosBrutos;
        
        try {
            InputStream in = getClass().getResourceAsStream("/templates/relatorio_participantes.html");
            if (in == null) {
                throw new RuntimeException("Template HTML não encontrado: relatorio_participantes.html");
            }
            byte[] bdata = FileCopyUtils.copyToByteArray(in);
            String html = new String(bdata, StandardCharsets.UTF_8);

            html = html.replace("$nomeEvento", evento.getTitulo());
            html = html.replace("$dataGeracao", LocalDateTime.now().toString());

            StringBuilder linhas = new StringBuilder();
            for (Participante p : participantes) {
                String raStr = p.getRA() != null ? p.getRA() : "Externo";
                linhas.append("<tr>");
                linhas.append("<td>").append(p.getNomeCompleto()).append("</td>");
                linhas.append("<td>").append(p.getEmail()).append("</td>");
                linhas.append("<td>").append(raStr).append("</td>");
                linhas.append("</tr>");
            }
            html = html.replace("$linhasTabela", linhas.toString());

            byte[] pdfBytes = pdfGenerator.gerarPDF(html);
            return new Relatorio(new java.util.Date(), "PARTICIPANTES_ATIVIDADE", pdfBytes, evento);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar PDF de relatório de participantes", e);
        }
    }
}
