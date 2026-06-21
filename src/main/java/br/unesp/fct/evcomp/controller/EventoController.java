package br.unesp.fct.evcomp.controller;

import br.unesp.fct.evcomp.domain.Evento;
import br.unesp.fct.evcomp.domain.Participante;
import br.unesp.fct.evcomp.domain.TipoContabilizacao;
import br.unesp.fct.evcomp.repository.EventoRepository;
import br.unesp.fct.evcomp.repository.ParticipanteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/eventos")

public class EventoController {

    private final EventoRepository eventoRepository;
    private final ParticipanteRepository participanteRepository;
    private final br.unesp.fct.evcomp.repository.SessaoRepository sessaoRepository;
    private final br.unesp.fct.evcomp.repository.InscricaoRepository inscricaoRepository;
    
    @Autowired
    private br.unesp.fct.evcomp.repository.AtividadeRepository atividadeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public EventoController(EventoRepository eventoRepository, ParticipanteRepository participanteRepository, br.unesp.fct.evcomp.repository.SessaoRepository sessaoRepository, br.unesp.fct.evcomp.repository.InscricaoRepository inscricaoRepository) {
        this.eventoRepository = eventoRepository;
        this.participanteRepository = participanteRepository;
        this.sessaoRepository = sessaoRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @GetMapping
    public ResponseEntity<List<Evento>> solicitarEventos() {
        return ResponseEntity.ok(eventoRepository.buscarTodosEventos());
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<Evento>> listarEventosDisponiveis() {
        return ResponseEntity.ok(eventoRepository.buscarEventosDisponiveis());
    }

    @GetMapping("/disponiveis/{participanteId}")
    public ResponseEntity<List<Evento>> solicitarEventosDisponiveis(@PathVariable Integer participanteId) {
        return ResponseEntity.ok(eventoRepository.buscarEventosDisponiveisPorParticipante(participanteId));
    }

    @GetMapping("/{id}/participantes")
    public ResponseEntity<List<Participante>> listarParticipantesDoEvento(@PathVariable Integer id) {
        List<Participante> participantes = inscricaoRepository.buscarParticipantesPorEvento(id);

        return ResponseEntity.ok(participantes);
    }

    @GetMapping("/coletor")
    public ResponseEntity<?> listarEventosDoColetor(jakarta.servlet.http.HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("auth_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Token não fornecido ou inválido."));
        }
        
        Optional<br.unesp.fct.evcomp.domain.Sessao> sessaoOpt = sessaoRepository.buscarSessaoPorToken(token);
        
        if (sessaoOpt.isPresent() && sessaoOpt.get().isAtiva()) {
            br.unesp.fct.evcomp.domain.Usuário userSessao = sessaoOpt.get().getUsuario();
            
            // Buscar o usuário fresquinho do banco para garantir que as coleções carreguem certo
            Optional<Participante> partOpt = participanteRepository.buscarParticipantePorId(userSessao.getId());
            
            if (partOpt.isPresent() && partOpt.get() instanceof br.unesp.fct.evcomp.domain.ColetorDePresenca) {
                br.unesp.fct.evcomp.domain.ColetorDePresenca coletor = (br.unesp.fct.evcomp.domain.ColetorDePresenca) partOpt.get();
                List<Evento> eventosColetados = coletor.getEventosColetados();
                
                // Filtra eventos que estão ocorrendo no momento
                LocalDate dataAtual = LocalDate.now();
                List<Evento> eventosAtivos = eventosColetados.stream()
                        .filter(e -> (e.getDataInicio() == null || !dataAtual.isBefore(e.getDataInicio())) && 
                                     (e.getDataFim() == null || !dataAtual.isAfter(e.getDataFim())))
                        .toList();
                        
                return ResponseEntity.ok(eventosAtivos);
            } else {
                return ResponseEntity.status(403).body(Map.of("error", "Usuário não é um Coletor"));
            }
        }
        return ResponseEntity.status(401).body(Map.of("error", "Sessão inválida ou expirada."));
    }

    @PostMapping("/{eventoId}/coletores/{participanteId}")
    public ResponseEntity<?> tornarColetor(@PathVariable Integer eventoId, @PathVariable("participanteId") Integer participanteId) {
        try {
            boolean ehColetor = participanteRepository.verificarColetor(participanteId, eventoId);
            
            if (ehColetor) {
                 return ResponseEntity.status(400).body(Map.of("error", "Participante já é coletor deste evento."));
            }
            
            participanteRepository.atribuirPapelColetor(participanteId, eventoId);
            entityManager.clear();
            
            return ResponseEntity.ok(Map.of("message", "Coletor associado com sucesso."));
        } catch (Exception e) {
            System.err.println("Erro ao associar coletor: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ocorreu um erro interno no servidor ao associar o coletor."));
        }
    }

    @DeleteMapping("/{eventoId}/coletores/{coletorId}")
    public ResponseEntity<?> removerColetor(@PathVariable String eventoId, @PathVariable String coletorId) {
        try {
            Integer evId = Integer.valueOf(eventoId);
            Integer usuId = Integer.valueOf(coletorId);
            
            // Remove do banco a associação na tabela N:M
            participanteRepository.removerColetorDoEventoNoBanco(usuId, evId);
            
            // Verifica se ficou sem eventos
            int count = participanteRepository.contarEventosDoColetor(usuId);
            if (count == 0) {
                // Downgrade: Volta a ser Participante comum
                participanteRepository.rebaixarParaParticipante(usuId);
            }
            
            entityManager.clear();
            
            return ResponseEntity.ok(Map.of("message", "Coletor removido com sucesso."));
        } catch (Exception e) {
            System.err.println("Erro ao remover coletor: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ocorreu um erro interno ao remover o coletor."));
        }
    }

    @GetMapping("/{eventoId}/detalhes")
    public ResponseEntity<?> selecionarEvento(@PathVariable Integer eventoId) {
        Optional<Evento> eventoEncontrado = eventoRepository.buscarEventoPorId(eventoId);

        if (!eventoEncontrado.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("error", "Evento não encontrado."));
        }

        Evento evento = eventoEncontrado.get();

        Map<String, Object> dadosEvento = evento.pegarDadosEvento();

        List<br.unesp.fct.evcomp.domain.Atividade> atividades = atividadeRepository.buscarAtividadesPorEvento(eventoId);
        
        return ResponseEntity.ok(Map.of(
            "dadosEvento", dadosEvento,
            "atividades", atividades
        ));
    }




    @PostMapping
    public ResponseEntity<?> confirmarCriacao(@RequestBody Map<String, String> req) {
        try {
            String titulo = req.get("titulo");
            String descricao = req.get("descricao");
            String link = req.get("link");
            String tipo = req.get("tipoContabilizacao");

            LocalDate dataInicio = LocalDate.parse(req.get("dataInicio"));
            LocalDate dataTermino = LocalDate.parse(req.get("dataTermino"));

            if (eventoRepository.verificarEventoCadastrado(titulo)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Já existe um evento cadastrado com este título."));
            }
            
            TipoContabilizacao tipoC = TipoContabilizacao.valueOf(tipo);
            
            Evento evento = Evento.criarEvento(titulo, dataInicio, dataTermino, descricao, link, tipoC);
           
            boolean eventoCriado = eventoRepository.salvarNovoEvento(evento);
            
            if (eventoCriado) {
                return ResponseEntity.ok(Map.of("message", "Evento criado com sucesso."));
            } else {
                return ResponseEntity.status(500).body(Map.of("error", "Não foi possível criar o evento."));
            }
        } catch (Exception e) {
            System.err.println("Erro ao criar evento: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ocorreu um erro interno no servidor ao cadastrar o evento."));
        }
    }

    private boolean verificarAlteracaoTitulo(String tituloEventoEncontrado, String titulo) {
        return titulo != null && !titulo.equals(tituloEventoEncontrado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> confirmarEdicao(@PathVariable Integer id, @RequestBody Map<String, String> req) {
        try {
            String titulo = req.get("titulo");
            String descricao = req.get("descricao");
            String link = req.get("link");
            String tipo = req.get("tipoContabilizacao");
            LocalDate dataInicio = LocalDate.parse(req.get("dataInicio"));
            LocalDate dataTermino = LocalDate.parse(req.get("dataTermino"));

            Optional<Evento> eventoEncontrado = eventoRepository.buscarEventoPorId(id);
            Evento evento = eventoEncontrado.get();
            
            if (verificarAlteracaoTitulo(evento.getTitulo(), titulo)) {
                if (eventoRepository.verificarEventoCadastrado(titulo)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Já existe um evento cadastrado com este título."));
                }
            }
            
            evento.setTitulo(titulo);
            evento.setDataInicio(dataInicio);
            evento.setDataFim(dataTermino);
            evento.setDescricao(descricao);
            evento.setLink(link);
            evento.setTipoContabilizacao(TipoContabilizacao.valueOf(tipo));
            
            boolean editado = eventoRepository.salvarEvento(evento);
            
            if (editado) {
                return ResponseEntity.ok(Map.of("message", "Evento editado com sucesso."));
            } else {
                return ResponseEntity.status(500).body(Map.of("error", "Erro ao tentar salvar o evento no banco."));
            }
        } catch (Exception e) {
            System.err.println("Erro ao editar evento: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ocorreu um erro interno no servidor ao editar o evento."));
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> confirmarConsulta(@RequestParam String tituloEvento) {
        java.util.List<Evento> listaEventos = eventoRepository.buscarEventosPorTituloParcial(tituloEvento);

        if(listaEventos.isEmpty()){
            return ResponseEntity.status(404).body(Map.of("error", "Nenhum evento encontrado com este título."));
        }

        return ResponseEntity.ok(listaEventos);
    }
}
