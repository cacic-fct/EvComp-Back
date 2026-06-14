package br.unesp.fct.evcomp.service.certificado;

import br.unesp.fct.evcomp.domain.Certificado;
import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.domain.Evento;
import br.unesp.fct.evcomp.domain.Atividade;

public interface CertificadoBuilder {
    void buildMetaDados(Participante participante, Evento evento, Atividade atividade, String tipo);
    void buildConteudo(int cargaHoraria, String papel, String detalheExtra);
    void buildPdfDocument();
    Certificado obterCertificado();
    byte[] getPdfBytes();
}
