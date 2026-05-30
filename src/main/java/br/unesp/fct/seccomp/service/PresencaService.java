package br.unesp.fct.seccomp.service;

import br.unesp.fct.seccomp.domain.Atividade;
import br.unesp.fct.seccomp.domain.Participante;
import br.unesp.fct.seccomp.domain.RegistroDePresenca;
import br.unesp.fct.seccomp.repository.PresencaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class PresencaService {

    private final PresencaRepository presencaRepository;

    @Autowired
    public PresencaService(PresencaRepository presencaRepository) {
        this.presencaRepository = presencaRepository;
    }

    public void salvarPresenca(String atividadeId, String participanteId) {
    }

    public String obterParticipanteId(String codigoParticipante) {
        return null;
    }

    public boolean validarCodigoParticipante(String codigoParticipante) {
        return false;
    }
}
