package br.unesp.fct.evcomp.service;

import br.unesp.fct.evcomp.domain.Atividade;
import br.unesp.fct.evcomp.repository.AtividadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AtividadeService {

    private final AtividadeRepository atividadeRepository;

    @Autowired
    public AtividadeService(AtividadeRepository atividadeRepository) {
        this.atividadeRepository = atividadeRepository;
    }

    public Optional<Atividade> buscarAtividade(Long id) {
        return atividadeRepository.findById(id);
    }

    public boolean checarPeriodoPresenca(Atividade atividade) {
        return false;
    }
}
