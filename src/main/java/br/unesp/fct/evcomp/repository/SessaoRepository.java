package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.Sessao;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SessaoRepository {

    private final Map<Integer, Sessao> sessoes = new ConcurrentHashMap<>();

    public Sessao save(Sessao sessao) {
        if (sessao.getId() == null) {
            sessao.setId(sessoes.size() + 1);
        }
        sessoes.put(sessao.getId(), sessao);
        return sessao;
    }

    public Optional<Sessao> buscarSessaoPorToken(String token) {
        return sessoes.values().stream()
                .filter(s -> s.getToken() != null && s.getToken().equals(token))
                .findFirst();
    }

    public Optional<Sessao> buscarSessaoPorUsuario(Integer usuarioId) {
        return sessoes.values().stream()
                .filter(s -> s.getUsuario() != null && s.getUsuario().getId().equals(usuarioId))
                .findFirst();
    }
}
