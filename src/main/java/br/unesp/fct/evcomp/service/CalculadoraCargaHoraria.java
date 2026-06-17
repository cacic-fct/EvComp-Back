package br.unesp.fct.evcomp.service;

import br.unesp.fct.evcomp.domain.Evento;
import org.springframework.stereotype.Service;

import java.util.List;
import br.unesp.fct.evcomp.domain.Atividade;
import br.unesp.fct.evcomp.repository.AtividadeRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class CalculadoraCargaHoraria {

    @Autowired
    private AtividadeRepository atividadeRepository;

    public int calcularCargaHorariaTotal(Evento evento) {
        int cargaHorariaTotal = 0;
        List<Atividade> atividades = atividadeRepository.buscarAtividadesPorEvento(evento.getId());
        for (Atividade a : atividades) {
            cargaHorariaTotal += a.getCargaHorariaTotal();
        }
        return cargaHorariaTotal;
    }
}
