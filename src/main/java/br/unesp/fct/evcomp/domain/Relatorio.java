package br.unesp.fct.evcomp.domain;

import java.util.Date;

public class Relatorio {

    private Integer id;

    private Date dataGeracao;

    private String tipoRelatorio;

    private byte[] pdfConteudo;

    public Relatorio() {
    }

    public Relatorio(Date dataGeracao, String tipoRelatorio, byte[] pdfConteudo) {
        this.dataGeracao = dataGeracao;
        this.tipoRelatorio = tipoRelatorio;
        this.pdfConteudo = pdfConteudo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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


}
