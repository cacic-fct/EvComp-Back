package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.Inscrição;
import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.domain.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface InscricaoRepository extends JpaRepository<Inscrição, Integer> {
    @org.springframework.data.jpa.repository.Query("SELECT i FROM Inscrição i WHERE i.participante.id = :participanteId AND i.evento.id = :eventoId")
    Optional<Inscrição> buscarPorParticipanteEEvento(@org.springframework.data.repository.query.Param("participanteId") Integer participanteId, @org.springframework.data.repository.query.Param("eventoId") Integer eventoId);
    
    @org.springframework.data.jpa.repository.Query("SELECT i.participante FROM Inscrição i WHERE i.evento.id = :eventoId AND i.status = true")
    List<Participante> findParticipantesByEventoId(@Param("eventoId") Integer eventoId);

    default boolean buscarPorParticipanteEEvento(String participanteId, String eventoId) {
        return buscarPorParticipanteEEvento(Integer.valueOf(participanteId), Integer.valueOf(eventoId)).isPresent();
    }

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(i) FROM Inscrição i JOIN i.atividade a WHERE a.id = :atividadeId")
    int contarInscritosPorAtividadeInt(@org.springframework.data.repository.query.Param("atividadeId") Integer atividadeId);

    default int contarInscritosPorAtividade(String atividadeId) { 
        return contarInscritosPorAtividadeInt(Integer.valueOf(atividadeId));
    }

    default void salvarInscricao(String participanteId, String eventoId, Atividade atividades) { }
    default Participante buscarParticipantesPorEvento(Participante participantes) { return null; }
    default boolean buscarPorParticipanteEAtividade(String participanteId, String atividadeId) { return false; }
}
