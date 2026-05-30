package br.unesp.fct.seccomp.service.relatorio;

import br.unesp.fct.seccomp.domain.Evento;
import br.unesp.fct.seccomp.domain.Participante;
import br.unesp.fct.seccomp.domain.Relatorio;
import br.unesp.fct.seccomp.repository.InscricaoRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Component
public class GraficoComparativoStrategy implements RelatorioStrategy {

    private final InscricaoRepository inscricaoRepository;

    @Autowired
    public GraficoComparativoStrategy(InscricaoRepository inscricaoRepository) {
        this.inscricaoRepository = inscricaoRepository;
    }

    @Override
    public Object processarDados(Evento dadosEvento) {
        List<Participante> participantes = inscricaoRepository.findParticipantesByEventoId(dadosEvento.getId());
        
        long internos = 0;
        long externos = 0;
        
        for (Participante p : participantes) {
            String email = p.getEmail().toLowerCase();
            if (email.endsWith("@unesp.br") || email.endsWith(".unesp.br") || email.contains("unesp.br")) {
                internos++;
            } else {
                externos++;
            }
        }
        
        Map<String, Long> resultado = new HashMap<>();
        resultado.put("internos", internos);
        resultado.put("externos", externos);
        return resultado;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Relatorio gerarPDF(Object dadosBrutos, Evento evento) {
        Map<String, Long> counts = (Map<String, Long>) dadosBrutos;
        long internos = counts.getOrDefault("internos", 0L);
        long externos = counts.getOrDefault("externos", 0L);
        long total = internos + externos;
        
        double pctInternos = total > 0 ? ((double) internos / total) * 100 : 0.0;
        double pctExternos = total > 0 ? ((double) externos / total) * 100 : 0.0;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
            Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);

            Paragraph title = new Paragraph("Relatório Comparativo de Participantes", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            Paragraph eventTitle = new Paragraph("Evento: " + evento.getTitulo(), sectionFont);
            eventTitle.setAlignment(Element.ALIGN_CENTER);
            eventTitle.setSpacingAfter(30);
            document.add(eventTitle);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(80);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.setWidths(new float[]{40f, 30f, 30f});

            table.addCell(new PdfPCell(new Paragraph("Categoria", sectionFont)));
            table.addCell(new PdfPCell(new Paragraph("Quantidade", sectionFont)));
            table.addCell(new PdfPCell(new Paragraph("Percentual", sectionFont)));

            table.addCell(new PdfPCell(new Paragraph("Internos (UNESP)", normalFont)));
            table.addCell(new PdfPCell(new Paragraph(String.valueOf(internos), normalFont)));
            table.addCell(new PdfPCell(new Paragraph(String.format("%.2f%%", pctInternos), normalFont)));

            table.addCell(new PdfPCell(new Paragraph("Externos (Outros)", normalFont)));
            table.addCell(new PdfPCell(new Paragraph(String.valueOf(externos), normalFont)));
            table.addCell(new PdfPCell(new Paragraph(String.format("%.2f%%", pctExternos), normalFont)));

            table.addCell(new PdfPCell(new Paragraph("Total Geral", sectionFont)));
            table.addCell(new PdfPCell(new Paragraph(String.valueOf(total), sectionFont)));
            table.addCell(new PdfPCell(new Paragraph("100.00%", sectionFont)));

            document.add(table);

            Paragraph chartTitle = new Paragraph("\nGráfico de Proporção (Barra Comparativa)", sectionFont);
            chartTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(chartTitle);

            int totalBlocks = 30;
            int internoBlocks = total > 0 ? (int) Math.round((double) internos / total * totalBlocks) : 0;
            int externoBlocks = totalBlocks - internoBlocks;

            StringBuilder bar = new StringBuilder();
            bar.append("[");
            for (int i = 0; i < internoBlocks; i++) {
                bar.append("=");
            }
            for (int i = 0; i < externoBlocks; i++) {
                bar.append("-");
            }
            bar.append("]");

            Paragraph barPara = new Paragraph(bar.toString(), new Font(Font.COURIER, 14, Font.BOLD));
            barPara.setAlignment(Element.ALIGN_CENTER);
            barPara.setSpacingBefore(10);
            document.add(barPara);

            Paragraph legendPara = new Paragraph("Legenda:  (=) Internos  |  (-) Externos", normalFont);
            legendPara.setAlignment(Element.ALIGN_CENTER);
            document.add(legendPara);

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            return new Relatorio(new java.util.Date(), "GRAFICO", pdfBytes, evento);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar PDF do relatório comparativo", e);
        }
    }
}
