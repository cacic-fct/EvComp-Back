package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.domain.Atividade;
import br.unesp.fct.evcomp.domain.Inscrição;
import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.domain.RegistroDePresenca;
import br.unesp.fct.evcomp.repository.AtividadeRepository;
import br.unesp.fct.evcomp.repository.InscricaoRepository;
import br.unesp.fct.evcomp.repository.ParticipanteRepository;
import br.unesp.fct.evcomp.repository.PresencaRepository;
import br.unesp.fct.evcomp.util.TOTPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/presencas")

public class PresencaController {

    private final AtividadeRepository atividadeRepository;
    private final ParticipanteRepository participanteRepository;
    private final InscricaoRepository inscricaoRepository;
    private final PresencaRepository presencaRepository;
    private final br.unesp.fct.evcomp.service.PresencaService presencaService;

    @Autowired
    public PresencaController(AtividadeRepository atividadeRepository, ParticipanteRepository participanteRepository, InscricaoRepository inscricaoRepository, PresencaRepository presencaRepository, br.unesp.fct.evcomp.service.PresencaService presencaService) {
        this.atividadeRepository = atividadeRepository;
        this.participanteRepository = participanteRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.presencaRepository = presencaRepository;
        this.presencaService = presencaService;
    }

    @Transactional
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarPresenca(@Valid @RequestBody br.unesp.fct.evcomp.dto.PresencaRequestDTO req) {
        try {
            Integer atividadeId = req.getAtividadeId();
            String codigoParticipante = req.getCodigoParticipante();
            long timestampLido = req.getTimestampLido();

            long horaAtual = System.currentTimeMillis();
            if (Math.abs(horaAtual - timestampLido) > (30 * 60 * 1000)) {
                return exibirMensagemErro("O timestamp da leitura possui defasagem excessiva. A sincronização falhou.", 403);
            }

            Integer participanteId = presencaService.processarCodigo(atividadeId, codigoParticipante, timestampLido);
            if (participanteId == null) {
                return exibirMensagemErro("Código de presença inválido ou expirado.", 403);
            }

            try {
                RegistroDePresenca presenca = presencaService.registrarEObterPresenca(atividadeId, participanteId);

                if (presenca == null) {
                    return exibirMensagemErro("Ocorreu um erro interno ao salvar a presença no banco de dados.", 500);
                }

                return exibirMensagemSucesso("Presença de " + presenca.getParticipante().getNomeCompleto() + " confirmada!", presenca);
            } catch (IllegalArgumentException e) {
                return exibirMensagemErro(e.getMessage(), 409);
            }

        } catch (Exception e) {
            return exibirMensagemErro("Erro interno no servidor ao processar o registro de presença.", 500);
        }
    }

    private ResponseEntity<?> exibirMensagemErro(String msg, int status) {
        return ResponseEntity.status(status).body(Map.of("error", msg));
    }

    private ResponseEntity<?> exibirMensagemSucesso(String msg, RegistroDePresenca data) {
        Map<String, Object> presencaDto = Map.of(
            "participante", Map.of(
                "nome", data.getParticipante().getNomeCompleto()
            )
        );
        return ResponseEntity.ok(Map.of("message", msg, "presenca", presencaDto));
    }

    @GetMapping("/participante/{participanteId}")
    public ResponseEntity<?> listarPresencasParticipante(@PathVariable Integer participanteId) {
        try {
            List<Integer> presencas = presencaRepository.buscarAtividadesComPresencaPorParticipante(participanteId);
            return ResponseEntity.ok(presencas);
        } catch (Exception e) {
            return exibirMensagemErro("Erro ao listar presenças", 500);
        }
    }
}
