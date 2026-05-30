package br.unesp.fct.seccomp.repository;

import br.unesp.fct.seccomp.domain.Usuário;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuário, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT u FROM Usuário u WHERE u.email = :email")
    Optional<Usuário> buscarUsuarioPorEmail(@org.springframework.data.repository.query.Param("email") String email);
    @org.springframework.data.jpa.repository.Query("SELECT u FROM Usuário u WHERE u.tokenRedefinicao = :tokenRedefinicao")
    Optional<Usuário> buscarPorTokenRedefinicao(@org.springframework.data.repository.query.Param("tokenRedefinicao") String tokenRedefinicao);

    default boolean validarEmailCadastrado(String email) {
        return false;
    }

    default boolean atualizarSenha(String novaSenha, String email) {
        return false;
    }
}
