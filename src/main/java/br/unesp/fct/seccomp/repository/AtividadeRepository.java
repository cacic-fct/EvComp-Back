package br.unesp.fct.seccomp.repository;

import br.unesp.fct.seccomp.domain.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Atividade a WHERE a.titulo = :titulo AND a.evento.id = :eventoId")
    Optional<Atividade> buscarAtividadePorTitulo(@org.springframework.data.repository.query.Param("titulo") String titulo, @org.springframework.data.repository.query.Param("eventoId") Long eventoId);
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Atividade a WHERE a.evento.id = :eventoId")
    List<Atividade> buscarAtividadesPorEvento(@org.springframework.data.repository.query.Param("eventoId") Long eventoId);

    default Atividade buscarAtividadePorId(String atividadeId) { return null; }
    default void removerAtividade(String atividadeId) { }
    default Atividade buscarAtividadesPorEvento(String eventoId) { return null; }
    default int consultarVagasDisponiveis(String atividadeId) { return 0; }
    default Atividade buscarAtividadesPorParticipante(String participanteId) { return null; }
    default boolean checarAndamentoAtividade(String atividadeId) { return false; }
    default int buscarCargaHorariaAtividade(String atividadeId) { return 0; }
    default Atividade salvarAtividade(Atividade atividade) { return null; }
    default Atividade buscarAtividadePorTitulo(String tituloAtividade) { return null; }
    default Atividade verificarAtividadeCadastrada(String tituloAtividade) { return null; }
}
