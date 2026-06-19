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
        String subtipo = "participante";
        String tipoCertificado = "GERAL";

        if (eventoId != null) {
            evento = eventoRepository.buscarEventoPorId(eventoId).orElse(null);
            if (evento != null) tipo = eventoRepository.buscarTipoEvento(eventoId);
        } else if (atividadeId != null) {
            atividade = atividadeRepository.buscarAtividadePorId(atividadeId).orElse(null);
            if (atividade != null) {
                evento = atividade.getEvento();
                tipo = atividadeRepository.buscarTipoEventoPorAtividade(atividadeId);
                tipoCertificado = "ATIVIDADE";
            }
        }

        if (evento == null) {
            dadosEmissao.put("error", "Evento ou atividade não encontrado(a).");
            return dadosEmissao;
        }

        if (atividade != null) {
            boolean isMinistrante = atividade.getMinistrantes().stream().anyMatch(m -> m.getId().equals(participante.getId()));
            if (isMinistrante) {
                subtipo = "ministrante";
                cargaHoraria = atividade.getCargaHorariaMinistrante();
            } else {
                boolean presente = presencaRepository.buscarPresencaPorAtividade(atividade.getId(), participante.getId())
                    .map(br.unesp.fct.evcomp.domain.RegistroDePresenca::isPresente)
                    .orElse(false);
                if (!presente) {
                    dadosEmissao.put("error", "Participante não possui presença confirmada nesta atividade.");
                    return dadosEmissao;
                }
                cargaHoraria = atividade.getCargaHorariaTotal();
            }
        } else {
            // Evento geral
            br.unesp.fct.evcomp.domain.Inscrição inscricao = inscricaoRepository.buscarPorParticipanteEEvento(participante.getId(), evento.getId()).orElse(null);
            if (inscricao == null || inscricao.getAtividade().isEmpty()) {
                dadosEmissao.put("error", "Participante não possui inscrições válidas neste evento.");
                return dadosEmissao;
            }

            int presentesCount = 0;
            for (Atividade at : inscricao.getAtividade()) {
                boolean presente = presencaRepository.buscarPresencaPorAtividade(at.getId(), participante.getId())
                    .map(br.unesp.fct.evcomp.domain.RegistroDePresenca::isPresente)
                    .orElse(false);
                if (presente) {
                    presentesCount++;
                    cargaHoraria += at.getCargaHorariaTotal();
                }
            }

            if (presentesCount == 0) {
                dadosEmissao.put("error", "Participante não possui presença em nenhuma atividade deste evento.");
                return dadosEmissao;
            }
        }

        dadosEmissao.put("evento", evento);
        if (atividade != null) dadosEmissao.put("atividade", atividade);
        dadosEmissao.put("cargaHoraria", cargaHoraria);
        dadosEmissao.put("subtipo", subtipo);
        dadosEmissao.put("tipoCertificado", tipoCertificado);

        return dadosEmissao;

    }

    public boolean verificarPresencaPorAtividade(Integer atividadeId, Integer participanteId) {
        return presencaRepository.buscarPresencaPorAtividade(atividadeId, participanteId).filter(br.unesp.fct.evcomp.domain.RegistroDePresenca::isPresente).isPresent();
    }

    @Transactional
    public byte[] gerarCertificado(Participante participante, java.util.Map<String, Object> dadosEmissao) {
        Evento evento = (Evento) dadosEmissao.get("evento");
        Atividade atividade = (Atividade) dadosEmissao.get("atividade");
        int cargaHoraria = (Integer) dadosEmissao.get("cargaHoraria");
        String tipoCertificado = (String) dadosEmissao.get("tipoCertificado");
        String subtipo = (String) dadosEmissao.get("subtipo");

        CertificadoBuilder builder = builderFactory.obterBuilder(tipoCertificado);

        builder.buildMetaDados(participante, evento, atividade, tipoCertificado);
        builder.buildConteudo(cargaHoraria, subtipo, null);
        builder.gerarPDF(pdfGenerator);

        Certificado cert = builder.obterCertificado();

        return builder.getPdfBytes();
    }
}
