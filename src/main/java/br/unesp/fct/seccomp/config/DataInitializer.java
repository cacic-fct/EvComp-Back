package br.unesp.fct.seccomp.config;

import br.unesp.fct.seccomp.domain.*;
import br.unesp.fct.seccomp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EventoRepository eventoRepository;
    private final InscricaoRepository inscricaoRepository;
    private final PresencaRepository presencaRepository;

    @Autowired
    public DataInitializer(UsuarioRepository usuarioRepository, EventoRepository eventoRepository, InscricaoRepository inscricaoRepository, PresencaRepository presencaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.eventoRepository = eventoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.presencaRepository = presencaRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Desativado
    }
}
