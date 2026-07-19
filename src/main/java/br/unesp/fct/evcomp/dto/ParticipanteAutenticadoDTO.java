package br.unesp.fct.evcomp.dto;

public class ParticipanteAutenticadoDTO extends ParticipanteResponseDTO {
    private String secretSeed;

    public ParticipanteAutenticadoDTO() {
        super();
    }

    public ParticipanteAutenticadoDTO(Integer id, String nomeCompleto, String email, String ra, String role, String secretSeed) {
        super(id, nomeCompleto, email, ra, role);
        this.secretSeed = secretSeed;
    }

    public static ParticipanteAutenticadoDTO fromEntity(br.unesp.fct.evcomp.domain.Usuário usuario) {
        if (usuario == null) return null;
        
        String ra = null;
        String seed = null;
        if (usuario instanceof br.unesp.fct.evcomp.domain.Participante) {
            br.unesp.fct.evcomp.domain.Participante p = (br.unesp.fct.evcomp.domain.Participante) usuario;
            ra = p.getRA();
            seed = p.getSecretSeed(); // Acessando método, @JsonIgnore afeta apenas Jackson
        }

        return new ParticipanteAutenticadoDTO(
            usuario.getId(),
            usuario.getNomeCompleto(),
            usuario.getEmail(),
            ra,
            usuario.getRole(),
            seed
        );
    }

    public static ParticipanteAutenticadoDTO fromEntity(br.unesp.fct.evcomp.domain.Participante participante) {
        return fromEntity((br.unesp.fct.evcomp.domain.Usuário) participante);
    }

    public String getSecretSeed() { return secretSeed; }
    public void setSecretSeed(String secretSeed) { this.secretSeed = secretSeed; }
}
