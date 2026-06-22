package br.unesp.fct.evcomp.service;

import br.unesp.fct.evcomp.domain.Atividade;
import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.domain.RegistroDePresenca;
import br.unesp.fct.evcomp.repository.PresencaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class PresencaService {

    private final PresencaRepository presencaRepository;

    private final QRCodeValidator qrCodeValidator;
    private final br.unesp.fct.evcomp.repository.InscricaoRepository inscricaoRepository;
    private final br.unesp.fct.evcomp.repository.ParticipanteRepository participanteRepository;
    private final br.unesp.fct.evcomp.repository.AtividadeRepository atividadeRepository;

    @Autowired
    public PresencaService(PresencaRepository presencaRepository, QRCodeValidator qrCodeValidator, 
                           br.unesp.fct.evcomp.repository.InscricaoRepository inscricaoRepository,
                           br.unesp.fct.evcomp.repository.ParticipanteRepository participanteRepository,
                           br.unesp.fct.evcomp.repository.AtividadeRepository atividadeRepository) {
        this.presencaRepository = presencaRepository;
        this.qrCodeValidator = qrCodeValidator;
        this.inscricaoRepository = inscricaoRepository;
        this.participanteRepository = participanteRepository;
        this.atividadeRepository = atividadeRepository;
    }

    public Integer processarCodigo(Integer atividadeId, String codigoParticipante, long timestampLido) {
        return qrCodeValidator.decodificarEValidar(atividadeId, codigoParticipante, timestampLido);
    }

    public RegistroDePresenca registrarEObterPresenca(Integer atividadeId, Integer participanteId) {
        if (presencaRepository.buscarPresencaPorAtividade(atividadeId, participanteId).isPresent()) {
            throw new IllegalArgumentException("Presença já registrada para este participante nesta atividade.");
        }
        
        Participante participante = participanteRepository.buscarParticipantePorId(participanteId).orElseThrow();
        Atividade atividade = atividadeRepository.buscarAtividadePorId(atividadeId).orElseThrow();
        
        RegistroDePresenca p = new RegistroDePresenca(new java.util.Date(), true, participante, atividade);

        return presencaRepository.salvarPresenca(p);
    }
}
