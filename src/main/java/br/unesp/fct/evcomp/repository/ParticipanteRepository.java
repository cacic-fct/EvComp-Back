package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.Participante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ParticipanteRepository extends JpaRepository<Participante, Integer> {
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
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE usuário SET tipo_usuario = 'COL' WHERE idUsuário = :id", nativeQuery = true)
    void tornarColetorNoBanco(@org.springframework.data.repository.query.Param("id") Integer id);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "INSERT IGNORE INTO coletor_presença (idUsuário, idEvento) VALUES (:idUsuario, :idEvento)", nativeQuery = true)
    void associarColetorAoEventoNoBanco(@org.springframework.data.repository.query.Param("idUsuario") Integer idUsuario, @org.springframework.data.repository.query.Param("idEvento") Integer idEvento);
}
