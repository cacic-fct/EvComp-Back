package br.unesp.fct.evcomp.domain;

import jakarta.persistence.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "atividade")
public class Atividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAtividade")
    private Integer id;

    @Column(nullable = false)
    private String titulo;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horarioInicio;

    @Column(name = "data_termino", nullable = false)
    private LocalDate dataFim;

    @Column(name = "hora_termino", nullable = false)
    private LocalTime horarioFim;

    @Column(name = "max_participantes", nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private int maxParticipantes;

    @Column(name = "carga_horaria_total", nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private int cargaHorariaTotal;

    @Column(name = "carga_horaria_ministrante", nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private int cargaHorariaMinistrante;

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEvento", nullable = false)
    private Evento evento;

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ministrante_atividade",
        joinColumns = @JoinColumn(name = "idAtividade"),
        inverseJoinColumns = @JoinColumn(name = "idUsuário")
    )
    private List<Usuário> ministrantes = new ArrayList<>();

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToMany(mappedBy = "atividade", fetch = FetchType.LAZY)
    private List<Inscrição> inscricoes = new ArrayList<>();

    @Transient
    private RegistroDePresenca[] registroDePresenca;

    @PreRemove
    private void removeInscricoes() {
        for (Inscrição inscricao : inscricoes) {
            inscricao.getAtividade().remove(this);
            if (inscricao.getAtividade().isEmpty()) {
                inscricao.setStatus(false);
            }
        }
    }

    public Atividade() {
    }

    public Atividade(String titulo, LocalDate dataInicio, LocalTime horarioInicio, LocalDate dataFim, LocalTime horarioFim, int maxParticipantes, int cargaHorariaTotal, int cargaHorariaMinistrante) {
        this.titulo = titulo;
        this.dataInicio = dataInicio;
        this.horarioInicio = horarioInicio;
        this.dataFim = dataFim;
        this.horarioFim = horarioFim;
        this.maxParticipantes = maxParticipantes;
        this.cargaHorariaTotal = cargaHorariaTotal;
        this.cargaHorariaMinistrante = cargaHorariaMinistrante;
    }

    public static Atividade criarAtividade(String titulo, LocalDate data_inicio, LocalTime horario_inicio, LocalDate data_termino, LocalTime horario_termino, int max_participantes, int carga_horaria_total, List<Participante> ministrantes, int carga_horaria_ministrantes) {
        Atividade atv = new Atividade(titulo, data_inicio, horario_inicio, data_termino, horario_termino, max_participantes, carga_horaria_total, carga_horaria_ministrantes);
        if (ministrantes != null) {
            atv.getMinistrantes().addAll(ministrantes);
        }
        return atv;
    }

    public Object pegarDadosAtividade(Atividade atividade) {
        return null;
    }

    public boolean alterarDadosAtividade(Object novosDadosAtividade) {
        return false;
    }


    public boolean verificarConflitoHorarios(Atividade outra) {
        LocalDateTime inicioEste = LocalDateTime.of(getDataInicio(), getHorarioInicio());
        LocalDateTime fimEste = LocalDateTime.of(getDataFim(), getHorarioFim());
        LocalDateTime inicioOutro = LocalDateTime.of(outra.getDataInicio(), outra.getHorarioInicio());
        LocalDateTime fimOutro = LocalDateTime.of(outra.getDataFim(), outra.getHorarioFim());

        return inicioEste.isBefore(fimOutro) && fimEste.isAfter(inicioOutro);
    }

    public boolean checarPeriodoPresenca() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicio = LocalDateTime.of(getDataInicio(), getHorarioInicio());
        LocalDateTime fim = LocalDateTime.of(getDataFim(), getHorarioFim());
        // Permite coleta de presença a partir de 10 minutos antes até o final da atividade
        return !agora.isBefore(inicio.minusMinutes(10)) && !agora.isAfter(fim);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public LocalTime getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(LocalTime horarioFim) {
        this.horarioFim = horarioFim;
    }

    public int getMaxParticipantes() {
        return maxParticipantes;
    }

    public void setMaxParticipantes(int maxParticipantes) {
        this.maxParticipantes = maxParticipantes;
    }

    public int getCargaHorariaTotal() {
        return cargaHorariaTotal;
    }

    public void setCargaHorariaTotal(int cargaHorariaTotal) {
        this.cargaHorariaTotal = cargaHorariaTotal;
    }

    public int getCargaHorariaMinistrante() {
        return cargaHorariaMinistrante;
    }

    public void setCargaHorariaMinistrante(int cargaHorariaMinistrante) {
        this.cargaHorariaMinistrante = cargaHorariaMinistrante;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public List<Usuário> getMinistrantes() {
        return ministrantes;
    }

    public void setMinistrantes(List<Usuário> ministrantes) {
        this.ministrantes = ministrantes;
    }

    public List<Inscrição> getInscricoes() {
        return inscricoes;
    }

    public void setInscricoes(List<Inscrição> inscricoes) {
        this.inscricoes = inscricoes;
    }

}
