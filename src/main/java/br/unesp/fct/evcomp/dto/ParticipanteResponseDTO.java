package br.unesp.fct.evcomp.dto;

/**
 * DTO de resposta para Participante.
 * Garante que apenas campos seguros sejam expostos via JSON,
 * evitando vazamento de secretSeed, senha_hash e tipo_usuario.
 */
public class ParticipanteResponseDTO {

    private Integer id;
    private String nomeCompleto;
    private String email;
    private String ra;
    private String role;

    public ParticipanteResponseDTO() {}

    public ParticipanteResponseDTO(Integer id, String nomeCompleto, String email, String ra, String role) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.ra = ra;
        this.role = role;
    }

    /**
     * Factory method para converter uma entidade Participante em DTO seguro.
     */
    public static ParticipanteResponseDTO fromEntity(br.unesp.fct.evcomp.domain.Participante participante) {
        return new ParticipanteResponseDTO(
            participante.getId(),
            participante.getNomeCompleto(),
            participante.getEmail(),
            participante.getRA(),
            participante.getRole()
        );
    }

    public static ParticipanteResponseDTO fromEntity(br.unesp.fct.evcomp.domain.Usuário usuario) {
        String ra = null;
        if (usuario instanceof br.unesp.fct.evcomp.domain.Participante) {
            ra = ((br.unesp.fct.evcomp.domain.Participante) usuario).getRA();
        }
        return new ParticipanteResponseDTO(
            usuario.getId(),
            usuario.getNomeCompleto(),
            usuario.getEmail(),
            ra,
            usuario.getRole()
        );
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRa() { return ra; }
    public void setRa(String ra) { this.ra = ra; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
