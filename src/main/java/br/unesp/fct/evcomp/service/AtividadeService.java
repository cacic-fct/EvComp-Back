package br.unesp.fct.evcomp.service;

import br.unesp.fct.evcomp.domain.Atividade;
import br.unesp.fct.evcomp.repository.AtividadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AtividadeService {

    private final AtividadeRepository atividadeRepository;
    private final br.unesp.fct.evcomp.repository.InscricaoRepository inscricaoRepository;

    @Autowired
    public AtividadeService(AtividadeRepository atividadeRepository, br.unesp.fct.evcomp.repository.InscricaoRepository inscricaoRepository) {
        this.atividadeRepository = atividadeRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    public Optional<Atividade> buscarAtividade(Integer id) {
        return atividadeRepository.findById(id);
    }

    public boolean checarPeriodoPresenca(Atividade atividade) {
        if (atividade == null) {
            return false;
        }
        return atividade.checarPeriodoPresenca();
    }

    public void verificarCapacidadeMinima(Integer atividadeId, int novoMax) {
        int inscritos = inscricaoRepository.contarInscritosPorAtividadeInt(atividadeId);

        if (novoMax < inscritos) {
            throw new IllegalArgumentException("O número máximo de participantes não pode ser inferior aos já inscritos (" + inscritos + " atualmente).");
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public int resolverConflitosDeHorario(Atividade at) {
        int desinscritos = 0;

        if (at.getDataInicio() != null && at.getHorarioInicio() != null && at.getDataFim() != null && at.getHorarioFim() != null) {
            java.util.List<br.unesp.fct.evcomp.domain.Inscrição> inscricoesParaVerificar = new java.util.ArrayList<>(at.getInscricoes());
            
            for (br.unesp.fct.evcomp.domain.Inscrição inscricao : inscricoesParaVerificar) {
                if (inscricao.isStatus()) {
                    boolean conflitoEncontrado = false;

                    for (br.unesp.fct.evcomp.domain.Atividade outra : inscricao.getAtividade()) {
                        if (!outra.getId().equals(at.getId()) && at.verificarConflitoHorarios(outra)) {
                            conflitoEncontrado = true;

                            break;
                        }
                    }

                    if (conflitoEncontrado) {
                        inscricao.getAtividade().remove(at);

                        at.getInscricoes().remove(inscricao);

                        if (inscricao.getAtividade().isEmpty()) {
                            inscricao.setStatus(false);
                        }

                        inscricaoRepository.salvarInscricao(inscricao);
                        
                        desinscritos++;
                    }
                }
            }
        }
        return desinscritos;
    }
}
