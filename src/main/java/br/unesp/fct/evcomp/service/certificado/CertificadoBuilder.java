package br.unesp.fct.evcomp.service.certificado;

import br.unesp.fct.evcomp.domain.Certificado;
import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.domain.Evento;
import br.unesp.fct.evcomp.domain.Atividade;
import br.unesp.fct.evcomp.service.PDFGenerator;

public interface CertificadoBuilder {
    void buildMetaDados(Participante participante, Evento evento, Atividade atividade, String tipo);
    void buildConteudo(int cargaHoraria, String papel, String detalheExtra);
    void gerarPDF(PDFGenerator pdfGenerator);
    Certificado obterCertificado();
    byte[] getPdfBytes();
}
