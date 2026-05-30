package br.unesp.fct.seccomp.domain;

import jakarta.persistence.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "atividades")
public class Atividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(name = "data_inicio", nullable = false)
    private Date dataInicio;

    @Column(name = "horario_inicio", nullable = false)
    private Date horarioInicio;

    @Column(name = "data_fim", nullable = false)
    private Date dataFim;

    @Column(name = "horario_fim", nullable = false)
    private Date horarioFim;

    @Column(name = "max_participantes", nullable = false)
    private int maxParticipantes;

    @Column(name = "carga_horaria_total", nullable = false)
    private int cargaHorariaTotal;

    @Column(name = "carga_horaria_ministrante", nullable = false)
    private int cargaHorariaMinistrante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "atividade_ministrantes",
        joinColumns = @JoinColumn(name = "atividade_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private List<Usuário> ministrantes = new ArrayList<>();

    @ManyToMany(mappedBy = "atividade", fetch = FetchType.LAZY)
    private List<Inscrição> inscricoes = new ArrayList<>();

    @Transient
    private RegistroDePresenca[] registroDePresenca;

    public Atividade() {
    }

    public Atividade(String titulo, Date dataInicio, Date horarioInicio, Date dataFim, Date horarioFim, int maxParticipantes, int cargaHorariaTotal, int cargaHorariaMinistrante) {
        this.titulo = titulo;
        this.dataInicio = dataInicio;
        this.horarioInicio = horarioInicio;
        this.dataFim = dataFim;
        this.horarioFim = horarioFim;
        this.maxParticipantes = maxParticipantes;
        this.cargaHorariaTotal = cargaHorariaTotal;
        this.cargaHorariaMinistrante = cargaHorariaMinistrante;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getDataInicio() {
        return dataInicio == null ? null : dataInicio.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio == null ? null : Date.from(dataInicio.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio == null ? null : horarioInicio.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime();
    }

    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio == null ? null : Date.from(horarioInicio.atDate(LocalDate.now()).atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    public LocalDate getDataFim() {
        return dataFim == null ? null : dataFim.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim == null ? null : Date.from(dataFim.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
    }

    public LocalTime getHorarioFim() {
        return horarioFim == null ? null : horarioFim.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime();
    }

    public void setHorarioFim(LocalTime horarioFim) {
        this.horarioFim = horarioFim == null ? null : Date.from(horarioFim.atDate(LocalDate.now()).atZone(java.time.ZoneId.systemDefault()).toInstant());
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
