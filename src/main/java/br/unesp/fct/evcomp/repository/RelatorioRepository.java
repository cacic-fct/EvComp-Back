package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.Relatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RelatorioRepository extends JpaRepository<Relatorio, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT r FROM Relatorio r WHERE r.evento.id = :eventoId")
    List<Relatorio> buscarRelatoriosPorEvento(@org.springframework.data.repository.query.Param("eventoId") Long eventoId);
}
