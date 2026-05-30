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

@Component
public class RelatorioParticipantesStrategy implements RelatorioStrategy {

    private final InscricaoRepository inscricaoRepository;

    @Autowired
    public RelatorioParticipantesStrategy(InscricaoRepository inscricaoRepository) {
        this.inscricaoRepository = inscricaoRepository;
    }

    @Override
    public Object processarDados(Evento dadosEvento) {
        return inscricaoRepository.findParticipantesByEventoId(dadosEvento.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Relatorio gerarPDF(Object dadosBrutos, Evento evento) {
        List<Participante> participantes = (List<Participante>) dadosBrutos;
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);

            Paragraph title = new Paragraph("Relatório de Participantes - " + evento.getTitulo(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            Paragraph date = new Paragraph("Gerado em: " + LocalDateTime.now(), normalFont);
            date.setSpacingAfter(20);
            document.add(date);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{30f, 45f, 25f});

            table.addCell(new PdfPCell(new Paragraph("Nome", headerFont)));
            table.addCell(new PdfPCell(new Paragraph("E-mail", headerFont)));
            table.addCell(new PdfPCell(new Paragraph("RA (Institucional)", headerFont)));

            for (Participante p : participantes) {
                table.addCell(new PdfPCell(new Paragraph(p.getNome(), normalFont)));
                table.addCell(new PdfPCell(new Paragraph(p.getEmail(), normalFont)));
                String raStr = p.getRA() != null ? p.getRA() : "Externo";
                table.addCell(new PdfPCell(new Paragraph(raStr, normalFont)));
            }

            document.add(table);
            document.close();

            byte[] pdfBytes = baos.toByteArray();
            return new Relatorio(new java.util.Date(), "PARTICIPANTES", pdfBytes, evento);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar PDF de relatório de participantes", e);
        }
    }
}
