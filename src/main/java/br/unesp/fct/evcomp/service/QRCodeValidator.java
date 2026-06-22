package br.unesp.fct.evcomp.service;

import org.springframework.stereotype.Service;

@Service
public class QRCodeValidator {
    private final br.unesp.fct.evcomp.repository.ParticipanteRepository participanteRepository;
    private final br.unesp.fct.evcomp.repository.InscricaoRepository inscricaoRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public QRCodeValidator(br.unesp.fct.evcomp.repository.ParticipanteRepository participanteRepository, 
                           br.unesp.fct.evcomp.repository.InscricaoRepository inscricaoRepository) {
        this.participanteRepository = participanteRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    public Integer decodificarEValidar(Integer atividadeId, String codigoParticipante, long timestampLido) {
        if (codigoParticipante == null || codigoParticipante.trim().isEmpty()) return null;

        String totp = codigoParticipante;
        Integer possivelId = null;

        // Tenta fazer o parse do JSON do QR Code
        if (codigoParticipante.startsWith("{") && codigoParticipante.endsWith("}")) {
            try {
                totp = codigoParticipante.split("\"t\":\"")[1].split("\"")[0];
                String pStr = codigoParticipante.split("\"p\":")[1].split("[,}]")[0].trim();
                possivelId = Integer.parseInt(pStr);
            } catch (Exception e) { return null; }
        }

        // Validação O(1) se for QR Code (já sabemos o dono)
        if (possivelId != null) {
            java.util.Optional<br.unesp.fct.evcomp.domain.Participante> participante = participanteRepository.buscarParticipantePorId(possivelId);
            if (participante.isPresent() && validarTOTP(participante.get().getSecretSeed(), totp, timestampLido)) {
                // Garante que está inscrito!
                if (inscricaoRepository.buscarPorParticipanteEAtividade(possivelId, atividadeId).isPresent()) {
                    return participante.get().getId();
                }
            }
            return null;
        }

        // Validação O(N) Fallback se for PIN Manual (Não sabemos quem é)
        if (codigoParticipante.length() == 6 && codigoParticipante.matches("\\d+")) {
            java.util.List<br.unesp.fct.evcomp.domain.Inscrição> inscricoes = inscricaoRepository.buscarInscricoesPorAtividade(atividadeId);
            for (br.unesp.fct.evcomp.domain.Inscrição inscricao : inscricoes) {
                // A lista acima JÁ É de inscritos!
                if (inscricao.isStatus() && validarTOTP(inscricao.getParticipante().getSecretSeed(), totp, timestampLido)) {
                    return inscricao.getParticipante().getId();
                }
            }
        }
        
        return null;
    }

    private boolean validarTOTP(String secretSeed, String codigoAuth, long timestampLido) {
        if (secretSeed == null) return false;
        if (br.unesp.fct.evcomp.util.TOTPUtil.validateTOTP(secretSeed, codigoAuth, timestampLido)) return true;
        if (br.unesp.fct.evcomp.util.TOTPUtil.validateTOTP(secretSeed, codigoAuth, timestampLido - 15000)) return true;
        if (br.unesp.fct.evcomp.util.TOTPUtil.validateTOTP(secretSeed, codigoAuth, timestampLido + 15000)) return true;
        return false;
    }
}
