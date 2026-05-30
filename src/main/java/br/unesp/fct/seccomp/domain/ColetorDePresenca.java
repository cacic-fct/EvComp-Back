package br.unesp.fct.seccomp.domain;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "coletores")
@PrimaryKeyJoinColumn(name = "participante_id")
public class ColetorDePresenca extends Participante {

    @Column(name = "data_atribuicao", nullable = false)
    private Date dataAtribuicao;

    public ColetorDePresenca() {
        super();
    }

    public Date getDataAtribuicao() {
        return dataAtribuicao;
    }

    public void setDataAtribuicao(Date dataAtribuicao) {
        this.dataAtribuicao = dataAtribuicao;
    }
}
