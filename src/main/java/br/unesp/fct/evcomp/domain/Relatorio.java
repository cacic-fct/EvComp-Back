package br.unesp.fct.evcomp.domain;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "relatorios")
public class Relatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_geracao", nullable = false)
    private Date dataGeracao;

    @Column(name = "tipo_relatorio", nullable = false)
    private String tipoRelatorio;

    @Lob
    @Column(name = "pdf_conteudo", columnDefinition = "LONGBLOB")
    private byte[] pdfConteudo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    public Relatorio() {
    }

    public Relatorio(Date dataGeracao, String tipoRelatorio, byte[] pdfConteudo, Evento evento) {
        this.dataGeracao = dataGeracao;
        this.tipoRelatorio = tipoRelatorio;
        this.pdfConteudo = pdfConteudo;
        this.evento = evento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(Date dataGeracao) {
        this.dataGeracao = dataGeracao;
    }

    public String getTipoRelatorio() {
        return tipoRelatorio;
    }

    public void setTipoRelatorio(String tipoRelatorio) {
        this.tipoRelatorio = tipoRelatorio;
    }

    public byte[] getPdfConteudo() {
        return pdfConteudo;
    }

    public void setPdfConteudo(byte[] pdfConteudo) {
        this.pdfConteudo = pdfConteudo;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }
}
