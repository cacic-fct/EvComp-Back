package br.unesp.fct.evcomp.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("ADM")
public class Administrador extends Usuário {

    public Administrador() {
        super();
    }

    public Administrador(String nomeCompleto, String email, String senha) {
        super(nomeCompleto, email, senha);
    }
}
