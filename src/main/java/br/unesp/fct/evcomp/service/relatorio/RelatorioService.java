package br.unesp.fct.evcomp.service.relatorio;

import br.unesp.fct.evcomp.domain.Evento;
import br.unesp.fct.evcomp.domain.Relatorio;
import br.unesp.fct.evcomp.domain.TipoRelatorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RelatorioService {

    private final RelatorioStrategyFactory strategyFactory;

    @Autowired
    public RelatorioService(RelatorioStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    public Relatorio gerarRelatorio(Object dadosEvento, TipoRelatorio tipoRelatorio) {
        if (!(dadosEvento instanceof Evento)) {
            throw new IllegalArgumentException("dadosEvento deve ser uma instância de Evento");
        }
        Evento evento = (Evento) dadosEvento;
        
        RelatorioStrategyFactory estrategia = strategyFactory.obterEstrategia(tipoRelatorio.name());
        Object dadosProcessados = estrategia.processarDados(evento);
        return estrategia.gerarPDF(dadosProcessados, evento);
    }

    public TipoRelatorio[] obterTiposRelatoriosDisponiveis() {
        return TipoRelatorio.values();
    }
}
