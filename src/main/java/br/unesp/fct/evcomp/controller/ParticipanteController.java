package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.dto.ParticipanteResponseDTO;
import br.unesp.fct.evcomp.repository.ParticipanteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/participantes")

public class ParticipanteController {

    private final ParticipanteRepository participanteRepository;

    @Autowired
    public ParticipanteController(ParticipanteRepository participanteRepository) {
        this.participanteRepository = participanteRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarParticipante(@PathVariable("id") Integer participanteId) {
        Participante participante = participanteRepository.buscarParticipantePorId(participanteId).orElse(null);
       
        if (participante == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Participante não encontrado"));
        }

        return ResponseEntity.ok(ParticipanteResponseDTO.fromEntity(participante));
    }

    @GetMapping
    public ResponseEntity<List<ParticipanteResponseDTO>> listarParticipantes() {
        List<ParticipanteResponseDTO> dtos = participanteRepository.findAll().stream()
            .map(p -> ParticipanteResponseDTO.fromEntity((Participante) p))
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarParticipante(@PathVariable("id") Integer participanteId, @RequestBody Map<String, String> body, jakarta.servlet.http.HttpServletRequest request) {
        // Proteção contra IDOR: só o próprio usuário ou um ADMIN pode editar seus dados
        Integer usuarioLogadoId = (Integer) request.getAttribute("usuarioLogadoId");
        String usuarioLogadoRole = (String) request.getAttribute("usuarioLogadoRole");

        if (!"ADMIN".equals(usuarioLogadoRole) && !participanteId.equals(usuarioLogadoId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Acesso negado. Você só pode editar seus próprios dados."));
        }

        String nomeCompleto = body.get("nome"); // Mantém 'nome' para compatibilidade com o frontend
        String ra = body.get("ra");
        
        boolean informacoesAlteradas = participanteRepository.salvarNovasInformacoesParticipante(participanteId, nomeCompleto, ra);
        
        if (informacoesAlteradas) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Informações atualizadas com sucesso"));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Participante não encontrado ou falha na atualização"));
        }
    }
}
