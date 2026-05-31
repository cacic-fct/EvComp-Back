package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CertificadoRepository extends JpaRepository<Certificado, Integer> {
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Certificado c WHERE c.usuario.id = :participanteId")
    List<Certificado> buscarCertificadosPorParticipante(@org.springframework.data.repository.query.Param("participanteId") Integer participanteId);
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Certificado c WHERE c.usuario.id = :participanteId AND c.atividade.evento.id = :eventoId")
    List<Certificado> buscarCertificadosPorParticipanteEEvento(@org.springframework.data.repository.query.Param("participanteId") Integer participanteId, @org.springframework.data.repository.query.Param("eventoId") Integer eventoId);
}
