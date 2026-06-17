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
    private br.unesp.fct.evcomp.repository.EventoRepository eventoRepository;
    
    @Autowired
    private br.unesp.fct.evcomp.service.CalculadoraCargaHoraria calculadoraCargaHoraria;

    @Autowired
    public CertificadoService(CertificadoBuilderFactory builderFactory, PresencaRepository presencaRepository, AtividadeRepository atividadeRepository, InscricaoRepository inscricaoRepository, br.unesp.fct.evcomp.service.PDFGenerator pdfGenerator) {
        this.builderFactory = builderFactory;
        this.presencaRepository = presencaRepository;
        this.atividadeRepository = atividadeRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.pdfGenerator = pdfGenerator;
    }

    public java.util.Map<String, Object> processarRegrasEmissao(Participante participante, Integer eventoId, Integer atividadeId) {
        java.util.Map<String, Object> dadosEmissao = new java.util.HashMap<>();
        Evento evento = null;
        Atividade atividade = null;
        TipoContabilizacao tipo = null;
        int cargaHoraria = 0;

        // 2.2.1 e 2.2.3: buscar evento
        if (eventoId != null) {
            evento = eventoRepository.buscarEventoPorIdInt(eventoId).orElse(null);
            if (evento != null) tipo = eventoRepository.buscarTipoEvento(eventoId);
        } else if (atividadeId != null) {
            atividade = atividadeRepository.findById(atividadeId).orElse(null);
            if (atividade != null) {
                evento = atividade.getEvento();
                tipo = atividadeRepository.buscarTipoEventoPorAtividade(atividadeId);
            }
        }

        if (evento == null) {
            dadosEmissao.put("error", "Evento ou atividade não encontrado(a).");
            return dadosEmissao;
        }

        dadosEmissao.put("evento", evento);
        if (atividade != null) dadosEmissao.put("atividade", atividade);

        // Validações de andamento e presença
        if (atividadeId != null) {
            if (atividadeRepository.checarAndamentoAtividade(String.valueOf(atividadeId)) || eventoRepository.checarAndamentoEvento(String.valueOf(evento.getId()))) {
                dadosEmissao.put("error", "A atividade (ou evento) ainda não foi finalizada.");
                return dadosEmissao;
            }
            boolean isMinistrante = atividade.getMinistrantes().stream().anyMatch(m -> m.getId().equals(participante.getId()));
            if (!isMinistrante && !verificarPresencaPorAtividade(String.valueOf(participante.getId()), String.valueOf(atividadeId))) {
                dadosEmissao.put("error", "Você não possui presença registrada nesta atividade.");
                return dadosEmissao;
            }
        } else {
            if (eventoRepository.checarAndamentoEvento(String.valueOf(evento.getId()))) {
                dadosEmissao.put("error", "O evento ainda não foi finalizado.");
                return dadosEmissao;
            }
            if (!verificarPresencaPorEvento(String.valueOf(participante.getId()), String.valueOf(evento.getId()))) {
                dadosEmissao.put("error", "Presença mínima não atingida neste evento.");
                return dadosEmissao;
            }
        }

        // 2.2.4 e 2.2.5: Carga horária baseada no tipo
        if (tipo != null && tipo.name().equals("POR_CARGA_TOTAL")) {
            cargaHoraria = calculadoraCargaHoraria.calcularCargaHorariaTotal(evento);
        } else if (tipo != null && tipo.name().equals("POR_ATIVIDADE") && atividadeId != null) {
            cargaHoraria = atividadeRepository.buscarCargaHorariaAtividade(atividadeId);
        }

        dadosEmissao.put("cargaHoraria", cargaHoraria);
        return dadosEmissao;
    }

    public boolean verificarPresencaPorEvento(String participanteId, String eventoId) {
        Integer eId = Integer.parseInt(eventoId);
        List<Atividade> atividades = atividadeRepository.buscarAtividadesPorEvento(eId);
        int totalAtividades = atividades.size();
        if (totalAtividades == 0) return true;

        int presencas = 0;
        for (Atividade a : atividades) {
            if (verificarPresencaPorAtividade(participanteId, String.valueOf(a.getId()))) {
                presencas++;
            }
        }
        double ratio = (double) presencas / totalAtividades;
        return ratio >= 0.5;
    }

    public boolean verificarPresencaPorAtividade(String participanteId, String atividadeId) {
        Integer aId = Integer.parseInt(atividadeId);
        Integer pId = Integer.parseInt(participanteId);
        return presencaRepository.buscarPresencaPorAtividade(aId, pId).filter(br.unesp.fct.evcomp.domain.RegistroDePresenca::isPresente).isPresent();
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
