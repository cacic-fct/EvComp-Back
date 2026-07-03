package br.unesp.fct.evcomp.repository;

import br.unesp.fct.evcomp.domain.Usuário;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuário, Integer> {
    @org.springframework.data.jpa.repository.Query("SELECT u FROM Usuário u WHERE u.email = :email")
    Optional<Usuário> buscarUsuarioPorEmail(@org.springframework.data.repository.query.Param("email") String email);

    default Optional<Usuário> validarEmailCadastrado(String email) {
        return buscarUsuarioPorEmail(email);
    }

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE Usuário u SET u.senha = :novaSenha WHERE u.email = :email")
    int updateSenha(@org.springframework.data.repository.query.Param("novaSenha") String novaSenha, @org.springframework.data.repository.query.Param("email") String email);

    default boolean atualizarSenha(String novaSenha, String email) {
        return updateSenha(novaSenha, email) > 0;
    }
}
