package br.unesp.fct.seccomp.repository;

import br.unesp.fct.seccomp.domain.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CertificadoRepository extends JpaRepository<Certificado, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Certificado c WHERE c.participante.id = :participanteId")
    List<Certificado> buscarCertificadosPorParticipante(@org.springframework.data.repository.query.Param("participanteId") Long participanteId);
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Certificado c WHERE c.participante.id = :participanteId AND c.evento.id = :eventoId")
    List<Certificado> buscarCertificadosPorParticipanteEEvento(@org.springframework.data.repository.query.Param("participanteId") Long participanteId, @org.springframework.data.repository.query.Param("eventoId") Long eventoId);
}
