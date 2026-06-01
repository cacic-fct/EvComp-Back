package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.Date;
import br.unesp.fct.evcomp.domain.TipoContabilizacao;

public interface EventoRepository extends JpaRepository<Evento, Integer> {
    @org.springframework.data.jpa.repository.Query("SELECT e FROM Evento e WHERE e.titulo = :titulo")
    Optional<Evento> buscarEventoPorTitulo(@org.springframework.data.repository.query.Param("titulo") String titulo);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(e) > 0 FROM Evento e WHERE e.titulo = :tituloEvento")
    boolean verificarEventoCadastrado(@org.springframework.data.repository.query.Param("tituloEvento") String tituloEvento);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Evento e WHERE e.id = :eventoId")
    Optional<Evento> buscarEventoPorIdInt(@org.springframework.data.repository.query.Param("eventoId") Integer eventoId);

    default Evento buscarEventoPorId(String eventoId) { 
        return buscarEventoPorIdInt(Integer.valueOf(eventoId)).orElse(null); 
    }

    default boolean checarAndamentoEvento(String eventoId) { 
        Optional<Evento> ev = buscarEventoPorIdInt(Integer.valueOf(eventoId));
        if (ev.isPresent() && ev.get().getDataFim() != null) {
            return ev.get().getDataFim().isBefore(java.time.LocalDate.now()); // ended
        }
        return false;
    }

    default Evento buscarEventosDisponiveis(String participanteId) { return null; }
    default Evento buscarTodosEventos() { return null; }
    default Evento buscarEventosPorParticipante(String participanteId) { return null; }
    default TipoContabilizacao buscarTipoEvento(String eventoId) { return null; }
    default TipoContabilizacao buscarTipoEventoPorAtividade(String atividadeId) { return null; }
    default boolean salvarEvento(String titulo, Date dataInicio, Date dataTermino, String descricao, String link) { return false; }
    default boolean atualizarDadosEvento(String titulo, Date dataInicio, Date dataTermino, String descricao, String link) { return false; }
}
