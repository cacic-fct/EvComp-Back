package br.unesp.fct.evcomp.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("ADM")
public class Administrador extends Usuário {

    public Administrador() {
        super();
    }

    public Administrador(String nome, String sobrenome, String email, String senha) {
        super(nome, sobrenome, email, senha);
    }
}
