package br.unesp.fct.evcomp.service;

import org.springframework.stereotype.Service;

import java.util.List;
import br.unesp.fct.evcomp.domain.Atividade;

@Service
public class CalculadoraCargaHoraria {

    public int calcularCargaHorariaTotal(List<Atividade> atividades) {
        int cargaHorariaTotal = 0;
        for (Atividade a : atividades) {
            cargaHorariaTotal += a.getCargaHorariaTotal();
        }
        return cargaHorariaTotal;
    }
}
