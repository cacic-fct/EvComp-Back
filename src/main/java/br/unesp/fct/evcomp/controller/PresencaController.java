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

    @Autowired
    public PresencaController(AtividadeRepository atividadeRepository, ParticipanteRepository participanteRepository, InscricaoRepository inscricaoRepository, PresencaRepository presencaRepository) {
        this.atividadeRepository = atividadeRepository;
        this.participanteRepository = participanteRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.presencaRepository = presencaRepository;
    }

    @Transactional
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarPresenca(@RequestBody Map<String, Object> req) {
        try {
            // 2. validarFormato
            if (!validarFormato(req)) {
                return exibirMensagemErro("Formato inválido. 'atividadeId', 'codigoAuth' e 'timestampLido' são obrigatórios.", 400);
            }

            Integer atividadeId = Integer.parseInt(String.valueOf(req.get("atividadeId")));
            String codigoAuth = String.valueOf(req.get("codigoAuth"));
            long timestampLido = Long.parseLong(String.valueOf(req.get("timestampLido")));

            // Validação de tolerância de Timestamp (Prevenção de fraude de Coletores)
            // Rejeita leitura que chegou com defasagem maior que 30 minutos em relação ao horário que foi gerada/lida
            // Isso permite a operação "internet lenta" / "offline temporário"
            long horaAtual = System.currentTimeMillis();
            if (Math.abs(horaAtual - timestampLido) > (30 * 60 * 1000)) {
                return exibirMensagemErro("O timestamp da leitura possui defasagem excessiva. A sincronização falhou (segurança).", 403);
            }

            Optional<Atividade> atvOpt = atividadeRepository.findById(atividadeId);
            if (atvOpt.isEmpty()) {
                return exibirMensagemErro("Atividade não encontrada.", 404);
            }
            Atividade atividade = atvOpt.get();

            // Buscar inscrições ativas na atividade para "brute-force" match no TOTP
            List<Inscrição> inscricoes = inscricaoRepository.buscarInscricoesPorAtividade(atividadeId);
            
            Participante participanteAutenticado = null;
            
            for (Inscrição inscricao : inscricoes) {
                if (inscricao.isStatus()) {
                    Participante p = inscricao.getParticipante();
                    // 5. validarCodigoParticipante
                    if (validarCodigoParticipante(p.getSecretSeed(), codigoAuth, timestampLido)) {
                        participanteAutenticado = p;
                        break;
                    }
                }
            }

            // 3. buscarParticipantePorId (conceitualmente aqui, localizamos o dono do código)
            if (participanteAutenticado == null) {
                return exibirMensagemErro("Código de presença inválido ou expirado para esta atividade.", 403);
            }

            // 4. buscarPorParticipanteEAtividade (Garantia extra, embora o brute force já prove)
            if (!isParticipanteInscrito(participanteAutenticado, atividadeId)) {
                return exibirMensagemErro("Participante não está inscrito nesta atividade.", 403);
            }

            // 6. buscarPresencaPorAtividade
            Optional<RegistroDePresenca> presencaExistente = presencaRepository.buscarPresencaPorAtividade(atividadeId, participanteAutenticado.getId());
            if (presencaExistente.isPresent()) {
                return exibirMensagemErro("Presença já registrada para este participante nesta atividade.", 409);
            }

            // 7. salvarPresenca
            RegistroDePresenca presenca = salvarPresenca(participanteAutenticado, atividade);

            // 9. exibirMensagemSucesso
            return exibirMensagemSucesso("Presença de " + participanteAutenticado.getNome() + " registrada com sucesso!", presenca);

        } catch (Exception e) {
            e.printStackTrace();
            return exibirMensagemErro("Erro interno no servidor ao processar o registro de presença.", 500);
        }
    }

    private boolean validarFormato(Map<String, Object> req) {
        return req.containsKey("atividadeId") && req.containsKey("codigoAuth") && req.containsKey("timestampLido");
    }

    private boolean validarCodigoParticipante(String secretSeed, String codigoAuth, long timestampLido) {
        if (secretSeed == null) return false;
        
        // Janela de tolerância do TOTP (Testa a janela exata lida pelo coletor, e uma janela anterior/posterior para delays de JS)
        // Isso resolve a latência sem permitir fraudes temporais grandes.
        if (TOTPUtil.validateTOTP(secretSeed, codigoAuth, timestampLido)) return true;
        if (TOTPUtil.validateTOTP(secretSeed, codigoAuth, timestampLido - 15000)) return true;
        if (TOTPUtil.validateTOTP(secretSeed, codigoAuth, timestampLido + 15000)) return true;
        
        return false;
    }

    private boolean isParticipanteInscrito(Participante participante, Integer atividadeId) {
        List<Inscrição> inscricoes = inscricaoRepository.buscarInscricoesAtivasPorParticipante(participante.getId());
        for (Inscrição i : inscricoes) {
            for (Atividade a : i.getAtividade()) {
                if (a.getId().equals(atividadeId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private RegistroDePresenca salvarPresenca(Participante participante, Atividade atividade) {
        RegistroDePresenca p = new RegistroDePresenca(new Date(), true, participante, atividade);
        return presencaRepository.save(p);
    }

    private ResponseEntity<?> exibirMensagemErro(String msg, int status) {
        return ResponseEntity.status(status).body(Map.of("error", msg));
    }

    private ResponseEntity<?> exibirMensagemSucesso(String msg, RegistroDePresenca data) {
        Map<String, Object> presencaDto = Map.of(
            "participante", Map.of(
                "nome", data.getParticipante().getNome()
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
