package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.RegistroDePresenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PresencaRepository extends JpaRepository<RegistroDePresenca, Integer> {

    @org.springframework.data.jpa.repository.Query("SELECT p FROM RegistroDePresenca p WHERE p.atividade.id = :atividadeId AND p.participante.id = :participanteId")
    Optional<RegistroDePresenca> buscarPresencaPorAtividade(@org.springframework.data.repository.query.Param("atividadeId") Integer atividadeId, @org.springframework.data.repository.query.Param("participanteId") Integer participanteId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(p) FROM RegistroDePresenca p JOIN p.atividade a WHERE p.participante.id = :participanteId AND a.evento.id = :eventoId AND p.presente = true")
    long contarPresencasNoEvento(@org.springframework.data.repository.query.Param("participanteId") Integer participanteId, @org.springframework.data.repository.query.Param("eventoId") Integer eventoId);

    default boolean buscarPresencaPorAtividade(String participanteId, String atividadeId) { return false; }
    default int contarPresencasNoEvento(String participanteId, String eventoId) { return 0; }
    default boolean salvarPresenca(String atividadeId, String participanteId) { return false; }
}
