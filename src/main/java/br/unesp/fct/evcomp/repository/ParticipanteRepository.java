package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.Participante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ParticipanteRepository extends JpaRepository<Participante, Integer> {
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Participante p WHERE p.email = :email")
    Optional<Participante> buscarPartcipantePorEmail(@org.springframework.data.repository.query.Param("email") String email);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Participante p WHERE p.RA = :ra")
    Optional<Participante> buscarPorRa(@org.springframework.data.repository.query.Param("ra") String ra);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Participante p WHERE p.id = :id")
    Optional<Participante> buscarParticipantePorId(@org.springframework.data.repository.query.Param("id") Integer id);

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
    void tornarColetor(@org.springframework.data.repository.query.Param("id") Integer id);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "INSERT IGNORE INTO coletor_presença (idUsuário, idEvento) VALUES (:idUsuario, :idEvento)", nativeQuery = true)
    void associarColetorAoEventoNoBanco(@org.springframework.data.repository.query.Param("idUsuario") Integer idUsuario, @org.springframework.data.repository.query.Param("idEvento") Integer idEvento);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM coletor_presença WHERE idUsuário = :idUsuario AND idEvento = :idEvento", nativeQuery = true)
    void removerColetorDoEventoNoBanco(@org.springframework.data.repository.query.Param("idUsuario") Integer idUsuario, @org.springframework.data.repository.query.Param("idEvento") Integer idEvento);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE usuário SET tipo_usuario = 'PAR' WHERE idUsuário = :id", nativeQuery = true)
    void rebaixarParaParticipante(@org.springframework.data.repository.query.Param("id") Integer id);

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM coletor_presença WHERE idUsuário = :idUsuario", nativeQuery = true)
    int contarEventosDoColetor(@org.springframework.data.repository.query.Param("idUsuario") Integer idUsuario);
}
