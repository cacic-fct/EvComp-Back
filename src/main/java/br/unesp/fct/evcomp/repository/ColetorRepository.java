package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.ColetorDePresenca;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColetorRepository extends JpaRepository<ColetorDePresenca, Long> {
    default boolean verificarColetor(String participanteId) { return false; }
    default void atribuirPapelColetor(String participanteId) { }
    default boolean deletarColetor(String eventoId, String coletorId) { return false; }
    default ColetorDePresenca buscarColetoresPorEvento(String eventoId) { return null; }
}
