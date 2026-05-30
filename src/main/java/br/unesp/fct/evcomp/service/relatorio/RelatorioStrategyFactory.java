package br.unesp.fct.evcomp.service.relatorio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RelatorioStrategyFactory {

    private final RelatorioParticipantesStrategy participantesStrategy;
    private final GraficoComparativoStrategy graficoStrategy;

    @Autowired
    public RelatorioStrategyFactory(RelatorioParticipantesStrategy participantesStrategy, GraficoComparativoStrategy graficoStrategy) {
        this.participantesStrategy = participantesStrategy;
        this.graficoStrategy = graficoStrategy;
    }

    public RelatorioStrategy obterEstrategia(String tipoRelatorio) {
        if ("PARTICIPANTES".equalsIgnoreCase(tipoRelatorio)) {
            return participantesStrategy;
        } else if ("GRAFICO".equalsIgnoreCase(tipoRelatorio)) {
            return graficoStrategy;
        }
        throw new IllegalArgumentException("Tipo de relatório desconhecido: " + tipoRelatorio);
    }
}
