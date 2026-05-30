package br.unesp.fct.evcomp.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "administradores")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Administrador extends Usuário {

    public Administrador() {
        super();
    }

    public Administrador(String nome, String email, String senha) {
        super(nome, email, senha);
    }
}
