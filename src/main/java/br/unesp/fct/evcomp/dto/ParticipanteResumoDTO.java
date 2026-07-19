package br.unesp.fct.evcomp.dto;

public class ParticipanteResumoDTO {
    private Integer id;
    private String nomeCompleto;

    public ParticipanteResumoDTO() {}

    public ParticipanteResumoDTO(Integer id, String nomeCompleto) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
    }

    public static ParticipanteResumoDTO fromEntity(br.unesp.fct.evcomp.domain.Usuário usuario) {
        if (usuario == null) return null;
        return new ParticipanteResumoDTO(
            usuario.getId(),
            usuario.getNomeCompleto()
        );
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
}
