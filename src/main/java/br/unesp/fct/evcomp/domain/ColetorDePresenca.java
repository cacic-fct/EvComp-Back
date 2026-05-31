package br.unesp.fct.evcomp.domain;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@DiscriminatorValue("COL")
public class ColetorDePresenca extends Participante {

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "coletor_presença",
        joinColumns = @JoinColumn(name = "idUsuário"),
        inverseJoinColumns = @JoinColumn(name = "idEvento")
    )
    private List<Evento> eventosColetados;

    public ColetorDePresenca() {
        super();
    }

    public List<Evento> getEventosColetados() {
        return eventosColetados;
    }

    public void setEventosColetados(List<Evento> eventosColetados) {
        this.eventosColetados = eventosColetados;
    }
}
