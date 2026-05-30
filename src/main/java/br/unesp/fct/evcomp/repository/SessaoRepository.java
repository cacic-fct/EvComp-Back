package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SessaoRepository extends JpaRepository<Sessao, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT s FROM Sessao s WHERE s.token = :token")
    Optional<Sessao> buscarSessaoPorToken(@org.springframework.data.repository.query.Param("token") String token);
    @org.springframework.data.jpa.repository.Query("SELECT s FROM Sessao s WHERE s.usuario.id = :usuarioId")
    Optional<Sessao> buscarSessaoPorUsuario(@org.springframework.data.repository.query.Param("usuarioId") Long usuarioId);
}
