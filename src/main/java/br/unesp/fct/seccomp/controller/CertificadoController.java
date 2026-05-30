package br.unesp.fct.seccomp.controller;

import br.unesp.fct.seccomp.domain.Atividade;
import br.unesp.fct.seccomp.domain.Certificado;
import br.unesp.fct.seccomp.domain.Evento;

import br.unesp.fct.seccomp.domain.Participante;
import br.unesp.fct.seccomp.repository.AtividadeRepository;
import br.unesp.fct.seccomp.repository.CertificadoRepository;
import br.unesp.fct.seccomp.repository.EventoRepository;
import br.unesp.fct.seccomp.repository.InscricaoRepository;
import br.unesp.fct.seccomp.repository.ParticipanteRepository;
import br.unesp.fct.seccomp.service.certificado.CertificadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/certificados")
@CrossOrigin(origins = "*")
public class CertificadoController {

    private final CertificadoService certificadoService;
    private final CertificadoRepository certificadoRepository;
    private final ParticipanteRepository participanteRepository;
    private final EventoRepository eventoRepository;
    private final AtividadeRepository atividadeRepository;
    private final InscricaoRepository inscricaoRepository;

    @Autowired
    public CertificadoController(CertificadoService certificadoService, CertificadoRepository certificadoRepository, ParticipanteRepository participanteRepository, EventoRepository eventoRepository, AtividadeRepository atividadeRepository, InscricaoRepository inscricaoRepository) {
        this.certificadoService = certificadoService;
        this.certificadoRepository = certificadoRepository;
        this.participanteRepository = participanteRepository;
        this.eventoRepository = eventoRepository;
        this.atividadeRepository = atividadeRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    public void selecionarEventoOuAtividade(String eventoId, String atividadeId) {}
    public void emitirCertificado(String participanteId, String eventoId, String atividadeId) {}
}
