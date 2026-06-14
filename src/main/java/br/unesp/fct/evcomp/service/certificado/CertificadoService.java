package br.unesp.fct.evcomp.service.certificado;

import br.unesp.fct.evcomp.domain.Atividade;
import br.unesp.fct.evcomp.domain.Certificado;
import br.unesp.fct.evcomp.domain.Evento;
import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.domain.TipoContabilizacao;
import br.unesp.fct.evcomp.repository.AtividadeRepository;
import br.unesp.fct.evcomp.repository.PresencaRepository;
import br.unesp.fct.evcomp.repository.InscricaoRepository;
import br.unesp.fct.evcomp.domain.Inscrição;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CertificadoService {

    private final CertificadoBuilderFactory builderFactory;
    private final PresencaRepository presencaRepository;
    private final AtividadeRepository atividadeRepository;
    private final InscricaoRepository inscricaoRepository;
    private final br.unesp.fct.evcomp.service.PDFGenerator pdfGenerator;

    @Autowired
    public CertificadoService(CertificadoBuilderFactory builderFactory, PresencaRepository presencaRepository, AtividadeRepository atividadeRepository, InscricaoRepository inscricaoRepository, br.unesp.fct.evcomp.service.PDFGenerator pdfGenerator) {
        this.builderFactory = builderFactory;
        this.presencaRepository = presencaRepository;
        this.atividadeRepository = atividadeRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.pdfGenerator = pdfGenerator;
    }

    public boolean verificarPresencaPorEvento(String participanteId, String eventoId) {
        return false;
    }

    public boolean verificarPresencaPorAtividade(String participanteId, String atividadeId) {
        return false;
    }

    @Transactional
    public byte[] gerarCertificado(Participante participante, Evento evento, Atividade atividade) {
        if (atividade != null) {
            boolean isMinistrante = atividade.getMinistrantes().stream().anyMatch(m -> m.getId().equals(participante.getId()));
            if (isMinistrante) {
                CertificadoBuilder builder = builderFactory.obterBuilder("ATIVIDADE");
                builder.buildMetaDados(participante, evento, atividade, "ATIVIDADE");
                builder.buildConteudo(atividade.getCargaHorariaMinistrante(), "ministrante", null);
                builder.gerarPDF(pdfGenerator);
                Certificado cert = builder.obterCertificado();
                return builder.getPdfBytes();
            }

            // Emissão por atividade individual
            boolean presente = presencaRepository.buscarPresencaPorAtividade(atividade.getId(), participante.getId())
                .map(p -> p.isPresente())
                .orElse(false);

            if (!presente) {
                throw new IllegalStateException("Participante não possui presença confirmada nesta atividade.");
            }

            CertificadoBuilder builder = builderFactory.obterBuilder("ATIVIDADE");
            builder.buildMetaDados(participante, evento, atividade, "ATIVIDADE");
            builder.buildConteudo(atividade.getCargaHorariaTotal(), "participante", null);
            builder.gerarPDF(pdfGenerator);
            Certificado cert = builder.obterCertificado();
            // Salvar no repositório se necessário
            return builder.getPdfBytes();
        } else {
            // Emissão geral do evento
            Inscrição inscricao = inscricaoRepository.buscarPorParticipanteEEvento(participante.getId(), evento.getId())
                    .orElseThrow(() -> new IllegalStateException("Participante não está inscrito neste evento."));

            List<Atividade> atividades = inscricao.getAtividade();
            if (atividades.isEmpty()) {
                throw new IllegalStateException("Participante não está inscrito em nenhuma atividade deste evento.");
            }

            long totalAtividades = atividades.size();
            long presentesCount = 0;
            int cargaHorariaTotalAcumulada = 0;

            for (Atividade at : atividades) {
                boolean presente = presencaRepository.buscarPresencaPorAtividade(at.getId(), participante.getId())
                    .map(p -> p.isPresente())
                    .orElse(false);
                if (presente) {
                    presentesCount++;
                }
                cargaHorariaTotalAcumulada += at.getCargaHorariaTotal();
            }

            if (presentesCount == 0) {
                throw new IllegalStateException("Participante não possui presença em nenhuma atividade deste evento.");
            }

            int cargaHorariaFinal = 0;
            if (evento.getTipoContabilizacao() == TipoContabilizacao.POR_CARGA_TOTAL) {
                double ratio = (double) presentesCount / totalAtividades;
                if (ratio == 1.0) {
                    cargaHorariaFinal = cargaHorariaTotalAcumulada;
                } else if (ratio >= 0.5) {
                    cargaHorariaFinal = cargaHorariaTotalAcumulada / 2;
                } else {
                    cargaHorariaFinal = 0;
                    throw new IllegalStateException("Frequência insuficiente para emissão do certificado geral (menor que 50%).");
                }
            } else {
                // POR_ATIVIDADE: Carga horária é a soma das atividades em que esteve presente
                for (Atividade at : atividades) {
                    boolean presente = presencaRepository.buscarPresencaPorAtividade(at.getId(), participante.getId())
                        .map(p -> p.isPresente())
                        .orElse(false);
                    if (presente) {
                        cargaHorariaFinal += at.getCargaHorariaTotal();
                    }
                }
            }

            CertificadoBuilder builder = builderFactory.obterBuilder("GERAL");
            builder.buildMetaDados(participante, evento, null, "GERAL");
            builder.buildConteudo(cargaHorariaFinal, "participante", null);
            builder.gerarPDF(pdfGenerator);
            Certificado cert = builder.obterCertificado();
            // Salvar no repositório se necessário
            return builder.getPdfBytes();
        }
    }
}
