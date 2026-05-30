package br.unesp.fct.evcomp.service.certificado;

import org.springframework.stereotype.Component;

@Component
public class CertificadoBuilderFactory {

    public CertificadoBuilder obterBuilder(String tipoCertificado) {
        if ("GERAL".equalsIgnoreCase(tipoCertificado)) {
            return new CertificadoGeralBuilder();
        } else if ("ATIVIDADE".equalsIgnoreCase(tipoCertificado)) {
            return new CertificadoAtividadeBuilder();
        }
        throw new IllegalArgumentException("Tipo de certificado desconhecido: " + tipoCertificado);
    }
}
