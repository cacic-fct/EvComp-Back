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
    private java.util.List<java.util.Map<String, Object>> eventosColetados;

    public ParticipanteResponseDTO() {}

    public ParticipanteResponseDTO(Integer id, String nomeCompleto, String email, String ra, String role) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.ra = ra;
        this.role = role;
    }

    public java.util.List<java.util.Map<String, Object>> getEventosColetados() { return eventosColetados; }
    public void setEventosColetados(java.util.List<java.util.Map<String, Object>> eventosColetados) { this.eventosColetados = eventosColetados; }

    public static ParticipanteResponseDTO fromEntity(br.unesp.fct.evcomp.domain.Participante participante) {
        ParticipanteResponseDTO dto = new ParticipanteResponseDTO(
            participante.getId(),
            participante.getNomeCompleto(),
            participante.getEmail(),
            participante.getRA(),
            participante.getRole()
        );
        
        if (participante instanceof br.unesp.fct.evcomp.domain.ColetorDePresenca) {
            br.unesp.fct.evcomp.domain.ColetorDePresenca coletor = (br.unesp.fct.evcomp.domain.ColetorDePresenca) participante;
            if (coletor.getEventosColetados() != null) {
                dto.setEventosColetados(coletor.getEventosColetados().stream()
                    .map(e -> java.util.Map.<String, Object>of("id", e.getId(), "titulo", e.getTitulo()))
                    .collect(java.util.stream.Collectors.toList()));
            }
        }
        return dto;
    }

    public static ParticipanteResponseDTO fromEntity(br.unesp.fct.evcomp.domain.Usuário usuario) {
        if (usuario instanceof br.unesp.fct.evcomp.domain.Participante) {
            return fromEntity((br.unesp.fct.evcomp.domain.Participante) usuario);
        }
        return new ParticipanteResponseDTO(
            usuario.getId(),
            usuario.getNomeCompleto(),
            usuario.getEmail(),
            null,
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
