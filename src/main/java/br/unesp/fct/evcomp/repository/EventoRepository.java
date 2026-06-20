package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import br.unesp.fct.evcomp.domain.TipoContabilizacao;


public interface EventoRepository extends JpaRepository<Evento, Integer> {
    @org.springframework.data.jpa.repository.Query("SELECT e FROM Evento e WHERE e.titulo = :titulo")
    Optional<Evento> buscarEventoPorTitulo(@org.springframework.data.repository.query.Param("titulo") String titulo);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Evento e WHERE LOWER(e.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    java.util.List<Evento> buscarEventosPorTituloParcial(@org.springframework.data.repository.query.Param("titulo") String titulo);

    default boolean verificarEventoCadastrado(String tituloEvento) {
        return buscarEventoPorTitulo(tituloEvento).isPresent();
    }

    default boolean salvarNovoEvento(Evento novoEvento) {
        try {
            save(novoEvento);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    default boolean salvarEvento(Evento evento) {
        try {
            save(evento);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Evento e WHERE e.id = :eventoId")
    Optional<Evento> buscarEventoPorId(@org.springframework.data.repository.query.Param("eventoId") Integer eventoId);


    default boolean checarAndamentoEvento(Integer eventoId) { 
        Optional<Evento> ev = buscarEventoPorId(eventoId);

        if (ev.isPresent()) {
            Evento evento = ev.get();

            if (evento.getDataFim() == null) return true;

            if (evento.getDataInicio() != null && evento.getDataInicio().equals(evento.getDataFim())) {
                return !java.time.LocalDate.now().isAfter(evento.getDataFim());
            } else {
                return evento.getDataFim().isAfter(java.time.LocalDate.now());
            }
        }

        return false;
    }

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Evento e WHERE e.dataFim >= CURRENT_DATE AND EXISTS (SELECT a FROM Atividade a WHERE a.evento.id = e.id)")
    java.util.List<Evento> buscarEventosDisponiveis();

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Evento e WHERE e.dataFim >= CURRENT_DATE AND EXISTS (SELECT a FROM Atividade a WHERE a.evento.id = e.id) AND e.id NOT IN (SELECT i.evento.id FROM Inscrição i WHERE i.participante.id = :participanteId AND i.status = true)")
    java.util.List<Evento> buscarEventosDisponiveisPorParticipante(@org.springframework.data.repository.query.Param("participanteId") Integer participanteId);

    default TipoContabilizacao buscarTipoEvento(Integer eventoId) {
        return findById(eventoId).map(Evento::getTipoContabilizacao).orElse(null);
    }

    default java.util.List<Evento> buscarTodosEventos() {
        return this.findAll();
    }
}
