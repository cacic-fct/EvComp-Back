package br.unesp.fct.evcomp.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("COL")
public class ColetorDePresenca extends Participante {

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "coletor_presença",
        joinColumns = @JoinColumn(name = "idUsuário"),
        inverseJoinColumns = @JoinColumn(name = "idEvento")
    )
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "atividades"})
    private List<Evento> eventosColetados = new ArrayList<>();

    public ColetorDePresenca() {
        super();
    }

    public List<Evento> getEventosColetados() {
        return eventosColetados;
    }

    public void setEventosColetados(List<Evento> eventosColetados) {
        this.eventosColetados = eventosColetados;
    }

    public boolean liberarAcesso() {
        return false;
    }
}
