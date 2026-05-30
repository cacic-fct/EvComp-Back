package br.unesp.fct.evcomp.service.certificado;

import br.unesp.fct.evcomp.domain.Atividade;
import br.unesp.fct.evcomp.domain.Certificado;
import br.unesp.fct.evcomp.domain.Evento;
import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.domain.TipoContabilizacao;
import br.unesp.fct.evcomp.repository.PresencaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificadoService {

    private final CertificadoBuilderFactory builderFactory;
    private final PresencaRepository presencaRepository;

    @Autowired
    public CertificadoService(CertificadoBuilderFactory builderFactory, PresencaRepository presencaRepository) {
        this.builderFactory = builderFactory;
        this.presencaRepository = presencaRepository;
    }

    public boolean verificarPresencaPorEvento(String participanteId, String eventoId) {
        return false;
    }

    public boolean verificarPresencaPorAtividade(String participanteId, String atividadeId) {
        return false;
    }

    public Certificado gerarCertificado(Object dadosEmissao) {
        return null;
    }

    public Object processarRegrasEmissao(Object dadosParticipante, String eventoId, String atividadeId) {
        return null;
    }

    public Certificado emitirCertificado(Participante participante, Evento evento, Atividade atividade) {
        if (atividade != null) {
            // Emissão por atividade individual
            boolean presente = presencaRepository.buscarPresencaPorAtividade(atividade.getId(), participante.getId())
                .map(p -> p.isPresente())
                .orElse(false);

            if (!presente) {
                throw new IllegalStateException("Participante não possui presença confirmada nesta atividade.");
            }

            CertificadoBuilder builder = builderFactory.obterBuilder("ATIVIDADE");
            builder.buildMetaDados(participante, evento, atividade, "ATIVIDADE");
            builder.buildConteudo(atividade.getCargaHorariaTotal(), "PARTICIPANTE", null);
            builder.buildPdfDocument();
            return builder.obterCertificado();
        } else {
            // Emissão geral do evento
            List<Atividade> atividades = new java.util.ArrayList<>();
            if (atividades.isEmpty()) {
                throw new IllegalStateException("Evento não possui atividades cadastradas.");
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
            builder.buildConteudo(cargaHorariaFinal, "PARTICIPANTE", null);
            builder.buildPdfDocument();
            return builder.obterCertificado();
        }
    }
}
