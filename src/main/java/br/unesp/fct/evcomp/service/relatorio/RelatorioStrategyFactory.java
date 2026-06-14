package br.unesp.fct.evcomp.service.relatorio;

import br.unesp.fct.evcomp.domain.Evento;
import br.unesp.fct.evcomp.domain.Relatorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;

@Component
@Primary
public class RelatorioStrategyFactory {

    @Autowired
    protected ApplicationContext applicationContext;

    public RelatorioStrategyFactory obterEstrategia(String tipoRelatorio) {
        if ("PARTICIPANTES".equalsIgnoreCase(tipoRelatorio)) {
            return applicationContext.getBean(RelatorioParticipantesStrategy.class);
        } else if ("GRAFICO".equalsIgnoreCase(tipoRelatorio)) {
            return applicationContext.getBean(GraficoComparativoStrategy.class);
        }
        throw new IllegalArgumentException("Tipo de relatório desconhecido: " + tipoRelatorio);
    }

    public Object processarDados(Evento dadosEvento) {
        return null;
    }

    public Relatorio gerarPDF(Object dadosBrutos, Evento evento) {
        return null;
    }
}
