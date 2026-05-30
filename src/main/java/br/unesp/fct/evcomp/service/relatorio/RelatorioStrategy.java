package br.unesp.fct.evcomp.service.relatorio;

import br.unesp.fct.evcomp.domain.Evento;
import br.unesp.fct.evcomp.domain.Relatorio;

public interface RelatorioStrategy {
    Object processarDados(Evento dadosEvento);
    Relatorio gerarPDF(Object dadosBrutos, Evento evento);
}
