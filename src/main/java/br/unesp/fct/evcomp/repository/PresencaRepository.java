package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.RegistroDePresenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PresencaRepository extends JpaRepository<RegistroDePresenca, Integer> {

    @Query("SELECT p FROM RegistroDePresenca p WHERE p.atividade.id = :atividadeId AND p.participante.id = :participanteId")
    Optional<RegistroDePresenca> buscarPresencaPorAtividade(@Param("atividadeId") Integer atividadeId, @Param("participanteId") Integer participanteId);

    @Query("SELECT COUNT(p) FROM RegistroDePresenca p JOIN p.atividade a WHERE p.participante.id = :participanteId AND a.evento.id = :eventoId AND p.presente = true")
    long contarPresencasNoEvento(@Param("participanteId") Integer participanteId, @Param("eventoId") Integer eventoId);

    @Query("SELECT p.atividade.id FROM RegistroDePresenca p WHERE p.participante.id = :participanteId AND p.presente = true")
    java.util.List<Integer> buscarAtividadesComPresencaPorParticipante(@Param("participanteId") Integer participanteId);
}
