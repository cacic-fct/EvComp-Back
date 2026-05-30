package br.unesp.fct.seccomp.repository;

import br.unesp.fct.seccomp.domain.Participante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ParticipanteRepository extends JpaRepository<Participante, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Participante p WHERE p.email = :email")
    Optional<Participante> buscarPartcipantePorEmail(@org.springframework.data.repository.query.Param("email") String email);

    default Participante buscarParticipantePorId(String participanteId) {
        return null;
    }

    default boolean salvarNovasInformacoesParticipante(String participanteId, String nome, String ra) {
        return false;
    }

    default Object pegarDadosParticipante(String participanteId) {
        return null;
    }

    default boolean verificarEmailCadastrado(String email) {
        return false;
    }

    default boolean salvarNovoParticipante(String nome, String email, String senha) {
        return false;
    }
}
