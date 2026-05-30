package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.Inscrição;
import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.domain.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface InscricaoRepository extends JpaRepository<Inscrição, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT i FROM Inscrição i WHERE i.participante.id = :participanteId AND i.evento.id = :eventoId")
    Optional<Inscrição> buscarPorParticipanteEEvento(@org.springframework.data.repository.query.Param("participanteId") Long participanteId, @org.springframework.data.repository.query.Param("eventoId") Long eventoId);
    
    @Query("SELECT i.participante FROM Inscrição i WHERE i.evento.id = :eventoId")
    List<Participante> findParticipantesByEventoId(@Param("eventoId") Long eventoId);

    default boolean buscarPorParticipanteEEvento(String participanteId, String eventoId) { return false; }
    default int contarInscritosPorAtividade(String atividadeId) { return 0; }
    default void salvarInscricao(String participanteId, String eventoId, Atividade atividades) { }
    default Participante buscarParticipantesPorEvento(Participante participantes) { return null; }
    default boolean buscarPorParticipanteEAtividade(String participanteId, String atividadeId) { return false; }
}
