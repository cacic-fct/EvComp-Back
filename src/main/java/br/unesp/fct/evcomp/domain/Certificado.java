package br.unesp.fct.evcomp.domain;

import jakarta.persistence.*;
import java.util.Date;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificado")
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCertificado")
    private Integer id;

    @Column(name = "data_emissao", nullable = false)
    private Date dataEmissao;

    @Column(name = "percentual_presenca", nullable = false)
    private float percentualPresenca;

    @Column(name = "tipo_certificado", nullable = false)
    private String tipoCertificado;

    @Column(name = "pdf_path")
    private String pdfPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuário", nullable = false)
    private Usuário usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAtividade", nullable = false)
    private Atividade atividade;

    public Certificado() {
    }

    public Certificado(Date dataEmissao, float percentualPresenca, String tipoCertificado, String pdfPath, Usuário usuario, Atividade atividade) {
        this.dataEmissao = dataEmissao;
        this.percentualPresenca = percentualPresenca;
        this.tipoCertificado = tipoCertificado;
        this.pdfPath = pdfPath;
        this.usuario = usuario;
        this.atividade = atividade;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public Usuário getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuário usuario) {
        this.usuario = usuario;
    }

    public Atividade getAtividade() {
        return atividade;
    }

    public void setAtividade(Atividade atividade) {
        this.atividade = atividade;
    }
}
