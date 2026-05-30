package br.unesp.fct.seccomp.repository;

import br.unesp.fct.seccomp.domain.ColetorDePresenca;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColetorRepository extends JpaRepository<ColetorDePresenca, Long> {
    default boolean verificarColetor(String participanteId) { return false; }
    default void atribuirPapelColetor(String participanteId) { }
    default boolean deletarColetor(String eventoId, String coletorId) { return false; }
    default ColetorDePresenca buscarColetoresPorEvento(String eventoId) { return null; }
}
