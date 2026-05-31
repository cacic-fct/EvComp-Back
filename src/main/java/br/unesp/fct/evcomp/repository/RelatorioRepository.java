package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.Relatorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class RelatorioRepository {

    private final Map<Integer, Relatorio> relatorios = new ConcurrentHashMap<>();

    public Relatorio save(Relatorio relatorio) {
        if (relatorio.getId() == null) {
            relatorio.setId(relatorios.size() + 1);
        }
        relatorios.put(relatorio.getId(), relatorio);
        return relatorio;
    }

    public List<Relatorio> buscarRelatoriosPorEvento(Integer eventoId) {
        return relatorios.values().stream()
                .filter(r -> r.getEvento() != null && r.getEvento().getId().equals(eventoId))
                .collect(Collectors.toList());
    }
}
