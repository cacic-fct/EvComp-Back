package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AtividadeRepository extends JpaRepository<Atividade, Integer> {
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Atividade a WHERE a.titulo = :titulo AND a.evento.id = :eventoId")
    Optional<Atividade> buscarAtividadePorTitulo(@org.springframework.data.repository.query.Param("titulo") String titulo, @org.springframework.data.repository.query.Param("eventoId") Integer eventoId);
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Atividade a WHERE a.evento.id = :eventoId")
    List<Atividade> buscarAtividadesPorEvento(@org.springframework.data.repository.query.Param("eventoId") Integer eventoId);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Atividade a WHERE a.titulo = :titulo AND a.evento.id = :eventoId")
    Optional<Atividade> buscarAtividadePorTituloEEvento(@org.springframework.data.repository.query.Param("titulo") String titulo, @org.springframework.data.repository.query.Param("eventoId") Integer eventoId);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Atividade a JOIN a.ministrantes m WHERE m.id = :usuarioId")
    List<Atividade> buscarAtividadesPorMinistrante(@org.springframework.data.repository.query.Param("usuarioId") Integer usuarioId);

    default Atividade verificarAtividadeCadastrada(String tituloAtividade, Integer eventoId) { 
        return buscarAtividadePorTituloEEvento(tituloAtividade, eventoId).orElse(null);
    }

    default Atividade buscarAtividadePorId(String atividadeId) { return null; }
    default void removerAtividade(String atividadeId) { }
    default Atividade buscarAtividadesPorEvento(String eventoId) { return null; }
    default int consultarVagasDisponiveis(String atividadeId) { return 0; }
    default Atividade buscarAtividadesPorParticipante(String participanteId) { return null; }
    default boolean checarAndamentoAtividade(String atividadeId) { 
        Optional<Atividade> atv = findById(Integer.valueOf(atividadeId));
        if (atv.isPresent()) {
            Atividade atividade = atv.get();
            if (atividade.getDataFim() == null) return true;
            return !java.time.LocalDate.now().isAfter(atividade.getDataFim());
        }
        return false;
    }
    default int buscarCargaHorariaAtividade(Integer atividadeId) { 
        return findById(atividadeId).map(Atividade::getCargaHorariaTotal).orElse(0); 
    }
    
    default br.unesp.fct.evcomp.domain.TipoContabilizacao buscarTipoEventoPorAtividade(Integer atividadeId) {
        return findById(atividadeId).map(a -> a.getEvento().getTipoContabilizacao()).orElse(null);
    }
    
    default Atividade salvarAtividade(Atividade atividade) { return null; }
    default Atividade buscarAtividadePorTitulo(String tituloAtividade) { return null; }
}
