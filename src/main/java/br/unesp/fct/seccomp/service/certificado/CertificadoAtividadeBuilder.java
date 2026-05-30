package br.unesp.fct.seccomp.service.certificado;

import br.unesp.fct.seccomp.domain.Certificado;
import br.unesp.fct.seccomp.domain.Participante;
import br.unesp.fct.seccomp.domain.Evento;
import br.unesp.fct.seccomp.domain.Atividade;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

public class CertificadoAtividadeBuilder implements CertificadoBuilder {

    private Certificado certificado;
    private Participante participante;
    private Evento evento;
    private Atividade atividade;
    private String tipo;

    private int cargaHoraria;
    private String papel;
    private String detalheExtra;

    private byte[] pdfBytes;

    @Override
    public void buildMetaDados(Participante participante, Evento evento, Atividade atividade, String tipo) {
        this.participante = participante;
        this.evento = evento;
        this.atividade = atividade;
        this.tipo = tipo;
    }

    @Override
    public void buildConteudo(int cargaHoraria, String papel, String detalheExtra) {
        this.cargaHoraria = cargaHoraria;
        this.papel = papel;
        this.detalheExtra = detalheExtra;
    }

    @Override
    public void buildPdfDocument() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 28, Font.BOLD);
            Font bodyFont = new Font(Font.TIMES_ROMAN, 16, Font.NORMAL);
            Font metaFont = new Font(Font.HELVETICA, 10, Font.ITALIC);

            Paragraph spacing = new Paragraph(" ");
            spacing.setSpacingBefore(30);

            Paragraph title = new Paragraph("CERTIFICADO DE ATIVIDADE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(40);
            document.add(title);
            document.add(spacing);

            String text = String.format(
                "Certificamos que %s concluiu a atividade \"%s\" pertencente ao evento \"%s\" com o papel de %s, obtendo uma carga horária específica de %d horas.",
                participante.getNome().toUpperCase(),
                atividade.getTitulo(),
                evento.getTitulo(),
                papel.toLowerCase(),
                cargaHoraria
            );
            Paragraph body = new Paragraph(text, bodyFont);
            body.setAlignment(Element.ALIGN_CENTER);
            body.setLeading(24);
            document.add(body);
            document.add(spacing);
            document.add(spacing);

            LocalDateTime agora = LocalDateTime.now();
            String dateText = String.format("Presidente Prudente, %02d/%02d/%d", agora.getDayOfMonth(), agora.getMonthValue(), agora.getYear());
            Paragraph datePara = new Paragraph(dateText, bodyFont);
            datePara.setAlignment(Element.ALIGN_CENTER);
            document.add(datePara);

            Paragraph signature = new Paragraph("_____________________________________\nComissão Organizadora - FCT UNESP", metaFont);
            signature.setAlignment(Element.ALIGN_CENTER);
            signature.setSpacingBefore(50);
            document.add(signature);

            document.close();
            this.pdfBytes = baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar PDF do certificado de atividade", e);
        }
    }

    @Override
    public Certificado obterCertificado() {
        float percentual = 100.0f;
        this.certificado = new Certificado(
            new java.util.Date(),
            percentual,
            tipo,
            pdfBytes,
            participante,
            evento,
            new Atividade[]{atividade}
        );
        return this.certificado;
    }
}
