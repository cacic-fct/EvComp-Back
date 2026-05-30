package br.unesp.fct.seccomp.service.certificado;

import br.unesp.fct.seccomp.domain.Certificado;
import br.unesp.fct.seccomp.domain.Participante;
import br.unesp.fct.seccomp.domain.Evento;
import br.unesp.fct.seccomp.domain.Atividade;

public interface CertificadoBuilder {
    void buildMetaDados(Participante participante, Evento evento, Atividade atividade, String tipo);
    void buildConteudo(int cargaHoraria, String papel, String detalheExtra);
    void buildPdfDocument();
    Certificado obterCertificado();
}
