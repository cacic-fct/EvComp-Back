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

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Participante p WHERE p.id IN :ids")
    java.util.List<Participante> buscarParticipantesPorId(@org.springframework.data.repository.query.Param("ids") java.util.List<Integer> ids);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE usuário SET tipo_usuario = 'COL' WHERE idUsuário = :usuarioId", nativeQuery = true)
    void atualizarTipoUsuarioParaColetor(@org.springframework.data.repository.query.Param("usuarioId") Integer usuarioId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "INSERT IGNORE INTO coletor_presença (idUsuário, idEvento) VALUES (:usuarioId, :eventoId)", nativeQuery = true)
    void associarColetorAoEvento(@org.springframework.data.repository.query.Param("usuarioId") Integer usuarioId, @org.springframework.data.repository.query.Param("eventoId") Integer eventoId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(c) > 0 FROM ColetorDePresenca c JOIN c.eventosColetados e WHERE c.id = :usuarioId AND e.id = :eventoId")
    boolean verificarColetor(@org.springframework.data.repository.query.Param("usuarioId") Integer usuarioId, @org.springframework.data.repository.query.Param("eventoId") Integer eventoId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM coletor_presença WHERE idUsuário = :idUsuario AND idEvento = :idEvento", nativeQuery = true)
    int removerColetorDoEvento(@org.springframework.data.repository.query.Param("idUsuario") Integer idUsuario, @org.springframework.data.repository.query.Param("idEvento") Integer idEvento);
    
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE usuário SET tipo_usuario = 'PAR' WHERE idUsuário = :id", nativeQuery = true)
    void rebaixarParaParticipante(@org.springframework.data.repository.query.Param("id") Integer id);
    
    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM coletor_presença WHERE idUsuário = :idUsuario", nativeQuery = true)
    int contarEventosDoColetor(@org.springframework.data.repository.query.Param("idUsuario") Integer idUsuario);
    
    
    @org.springframework.transaction.annotation.Transactional
    default void atribuirPapelColetor(Integer usuarioId, Integer eventoId) {
        atualizarTipoUsuarioParaColetor(usuarioId);
        associarColetorAoEvento(usuarioId, eventoId);
    }

    @org.springframework.transaction.annotation.Transactional
    default boolean deletarColetor(Integer eventoId, Integer coletorId) {
        int removidos = removerColetorDoEvento(coletorId, eventoId);
        if (removidos > 0) {
            int restantes = contarEventosDoColetor(coletorId);
            if (restantes == 0) {
                rebaixarParaParticipante(coletorId);
            }
            return true;
        }
        return false;
    }

    default boolean salvarNovasInformacoesParticipante(Integer participanteId, String nomeCompleto, String ra) {
        Optional<Participante> p = buscarParticipantePorId(participanteId);

        if (p.isPresent()) {
            Participante part = p.get();
            part.setNomeCompleto(nomeCompleto);
            part.setRA(ra);
            save(part);
            return true;
        }
        return false;
    }

    default boolean verificarEmailCadastrado(String email) {
        return buscarPartcipantePorEmail(email).isPresent();
    }

    default void salvarNovoParticipante(Participante novoParticipante) {
        save(novoParticipante);
    }
}
