package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.Date;
import br.unesp.fct.evcomp.domain.TipoContabilizacao;

public interface EventoRepository extends JpaRepository<Evento, Integer> {
    @org.springframework.data.jpa.repository.Query("SELECT e FROM Evento e WHERE e.titulo = :titulo")
    Optional<Evento> buscarEventoPorTitulo(@org.springframework.data.repository.query.Param("titulo") String titulo);

    default Evento buscarEventoPorId(String eventoId) { return null; }
    default Evento buscarEventosDisponiveis(String participanteId) { return null; }
    default Evento buscarTodosEventos() { return null; }
    default Evento buscarEventosPorParticipante(String participanteId) { return null; }
    default boolean checarAndamentoEvento(String eventoId) { return false; }
    default TipoContabilizacao buscarTipoEvento(String eventoId) { return null; }
    default TipoContabilizacao buscarTipoEventoPorAtividade(String atividadeId) { return null; }
    default boolean verificarEventoCadastrado(String tituloEvento) { return false; }
    default boolean salvarEvento(String titulo, Date dataInicio, Date dataTermino, String descricao, String link) { return false; }
    default boolean atualizarDadosEvento(String titulo, Date dataInicio, Date dataTermino, String descricao, String link) { return false; }
}
