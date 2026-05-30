package br.unesp.fct.seccomp.domain;

import jakarta.persistence.*;
import java.util.Date;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificados")
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_emissao", nullable = false)
    private Date dataEmissao;

    @Column(name = "percentual_presenca", nullable = false)
    private float percentualPresenca;

    @Column(name = "tipo_certificado", nullable = false)
    private String tipoCertificado;

    @Lob
    @Column(name = "pdf_conteudo", columnDefinition = "LONGBLOB")
    private byte[] pdfConteudo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participante_id", nullable = false)
    private Participante participante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Transient
    private Atividade[] atividade;

    public Certificado() {
    }

    public Certificado(Date dataEmissao, float percentualPresenca, String tipoCertificado, byte[] pdfConteudo, Participante participante, Evento evento, Atividade[] atividade) {
        this.dataEmissao = dataEmissao;
        this.percentualPresenca = percentualPresenca;
        this.tipoCertificado = tipoCertificado;
        this.pdfConteudo = pdfConteudo;
        this.participante = participante;
        this.evento = evento;
        this.atividade = atividade;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataEmissao() {
        return dataEmissao == null ? null : dataEmissao.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
    }

    public void setDataEmissao(LocalDateTime dataEmissao) {
        this.dataEmissao = dataEmissao == null ? null : Date.from(dataEmissao.atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    public float getPercentualPresenca() {
        return percentualPresenca;
    }

    public void setPercentualPresenca(float percentualPresenca) {
        this.percentualPresenca = percentualPresenca;
    }

    public String getTipoCertificado() {
        return tipoCertificado;
    }

    public void setTipoCertificado(String tipoCertificado) {
        this.tipoCertificado = tipoCertificado;
    }

    public byte[] getPdfConteudo() {
        return pdfConteudo;
    }

    public void setPdfConteudo(byte[] pdfConteudo) {
        this.pdfConteudo = pdfConteudo;
    }

    public Participante getParticipante() {
        return participante;
    }

    public void setParticipante(Participante participante) {
        this.participante = participante;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public Atividade[] getAtividade() {
        return atividade;
    }

    public void setAtividade(Atividade[] atividade) {
        this.atividade = atividade;
    }
}
