package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.Usuário;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuário, Integer> {
    @org.springframework.data.jpa.repository.Query("SELECT u FROM Usuário u WHERE u.email = :email")
    Optional<Usuário> buscarUsuarioPorEmail(@org.springframework.data.repository.query.Param("email") String email);


    default boolean validarEmailCadastrado(String email) {
        return false;
    }

    default boolean atualizarSenha(String novaSenha, String email) {
        return false;
    }
}
