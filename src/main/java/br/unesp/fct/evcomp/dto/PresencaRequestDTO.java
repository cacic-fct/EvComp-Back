package br.unesp.fct.evcomp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PresencaRequestDTO {

    @NotNull(message = "O ID da atividade é obrigatório.")
    private Integer atividadeId;

    @NotBlank(message = "O código do participante é obrigatório.")
    private String codigoParticipante;

    @NotNull(message = "O timestamp da leitura é obrigatório.")
    private Long timestampLido;

    public Integer getAtividadeId() { return atividadeId; }
    public void setAtividadeId(Integer atividadeId) { this.atividadeId = atividadeId; }

    public String getCodigoParticipante() { return codigoParticipante; }
    public void setCodigoParticipante(String codigoParticipante) { this.codigoParticipante = codigoParticipante; }

    public Long getTimestampLido() { return timestampLido; }
    public void setTimestampLido(Long timestampLido) { this.timestampLido = timestampLido; }
}
