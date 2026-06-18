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

    default Atividade verificarAtividadeCadastrada(String tituloAtividade, Integer eventoId) { 
        return buscarAtividadePorTituloEEvento(tituloAtividade, eventoId).orElse(null);
    }
    
    @org.springframework.data.jpa.repository.Query("SELECT a.maxParticipantes - (SELECT COUNT(i) FROM Inscrição i JOIN i.atividade atv WHERE atv.id = a.id AND i.status = true) FROM Atividade a WHERE a.id = :atividadeId")
    Integer consultarVagasDisponiveis(@org.springframework.data.repository.query.Param("atividadeId") Integer atividadeId);
    
    default Atividade buscarAtividadePorId(String atividadeId) { return null; }
    default void removerAtividade(String atividadeId) { }
    default Atividade buscarAtividadesPorEvento(String eventoId) { return null; }
    default Atividade buscarAtividadesPorParticipante(String participanteId) { return null; }
    
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Atividade a JOIN a.ministrantes m WHERE m.id = :ministranteId")
    List<Atividade> buscarAtividadesPorMinistrante(@org.springframework.data.repository.query.Param("ministranteId") Integer ministranteId);
    default boolean checarAndamentoAtividade(String atividadeId) { return false; }
    default int buscarCargaHorariaAtividade(Integer atividadeId) { 
        return findById(atividadeId).map(Atividade::getCargaHorariaTotal).orElse(0); 
    }
    
    default br.unesp.fct.evcomp.domain.TipoContabilizacao buscarTipoEventoPorAtividade(Integer atividadeId) {
        return findById(atividadeId).map(a -> a.getEvento().getTipoContabilizacao()).orElse(null);
    }
    default Atividade salvarAtividade(Atividade atividade) { return null; }
    default Atividade buscarAtividadePorTitulo(String tituloAtividade) { return null; }
}
