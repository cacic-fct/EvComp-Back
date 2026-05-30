package br.unesp.fct.seccomp.service.relatorio;

import br.unesp.fct.seccomp.domain.Evento;
import br.unesp.fct.seccomp.domain.Relatorio;

public interface RelatorioStrategy {
    Object processarDados(Evento dadosEvento);
    Relatorio gerarPDF(Object dadosBrutos, Evento evento);
}
