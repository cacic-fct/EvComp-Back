package br.unesp.fct.seccomp.repository;

import br.unesp.fct.seccomp.domain.RegistroDePresenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PresencaRepository extends JpaRepository<RegistroDePresenca, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT p FROM RegistroDePresenca p WHERE p.atividade.id = :atividadeId AND p.participante.id = :participanteId")
    Optional<RegistroDePresenca> buscarPresencaPorAtividade(@org.springframework.data.repository.query.Param("atividadeId") Long atividadeId, @org.springframework.data.repository.query.Param("participanteId") Long participanteId);
    

    @org.springframework.data.jpa.repository.Query("SELECT count(p) FROM RegistroDePresenca p WHERE p.participante.id = :participanteId AND p.atividade.evento.id = :eventoId AND p.presente = true")
    long contarPresencasNoEvento(@org.springframework.data.repository.query.Param("participanteId") Long participanteId, @org.springframework.data.repository.query.Param("eventoId") Long eventoId);

    default boolean buscarPresencaPorAtividade(String participanteId, String atividadeId) { return false; }
    default int contarPresencasNoEvento(String participanteId, String eventoId) { return 0; }
    default boolean salvarPresenca(String atividadeId, String participanteId) { return false; }
}
