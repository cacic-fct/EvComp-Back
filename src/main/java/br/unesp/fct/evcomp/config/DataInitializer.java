package br.unesp.fct.evcomp.config;

import br.unesp.fct.evcomp.domain.*;
import br.unesp.fct.evcomp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EventoRepository eventoRepository;
    private final AtividadeRepository atividadeRepository;
    private final ParticipanteRepository participanteRepository;

    @Autowired
    public DataInitializer(UsuarioRepository usuarioRepository, EventoRepository eventoRepository, 
                           AtividadeRepository atividadeRepository, ParticipanteRepository participanteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.eventoRepository = eventoRepository;
        this.atividadeRepository = atividadeRepository;
        this.participanteRepository = participanteRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Verifica se já existem dados para não duplicar em re-runs
        if (eventoRepository.count() == 0) {
            
            // 1. Criar Evento Base
            Evento evento = new Evento(
                "SECOMPP 2026 - Semana da Computação",
                new Date(),
                new Date(System.currentTimeMillis() + 86400000L * 5), // +5 dias
                "Maior evento de tecnologia do Oeste Paulista",
                "https://fct.unesp.br/evcomp",
                TipoContabilizacao.POR_ATIVIDADE
            );
            evento = eventoRepository.save(evento);

            // 2. Criar Atividade Base
            Atividade atividade = new Atividade(
                "Minicurso: Inteligência de Enxames aplicada a Robótica",
                new Date(),
                new Date(),
                new Date(System.currentTimeMillis() + 86400000L * 5),
                new Date(),
                50,
                4,
                4
            );
            atividade.setEvento(evento);
            atividadeRepository.save(atividade);

            // 3. Criar Administrador de Teste
            Administrador admin = new Administrador("Admin", "FCT", "admin@unesp.br", "123456");
            usuarioRepository.save(admin);

            // 4. Criar Participante (Usuário Comum)
            Participante part = new Participante("João", "Silva", "joao@unesp.br", "123456", "123456789");
            participanteRepository.save(part);
            
            System.out.println("========== MOCK DATA INITIALIZED SUCCESSFULLY (H2) ==========");
        }
    }
}
