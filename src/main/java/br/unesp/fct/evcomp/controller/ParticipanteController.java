package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.domain.Participante;
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

        return ResponseEntity.ok(participante);
    }

    @GetMapping
    public ResponseEntity<List<Participante>> listarParticipantes() {
        return ResponseEntity.ok(participanteRepository.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarParticipante(@PathVariable("id") Integer participanteId, @RequestBody Map<String, String> body) {
        String nome = body.get("nome");
        String ra = body.get("ra");
        
        boolean informacoesAlteradas = participanteRepository.salvarNovasInformacoesParticipante(participanteId, nome, ra);
        
        if (informacoesAlteradas) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Informações atualizadas com sucesso"));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Participante não encontrado ou falha na atualização"));
        }
    }
}
