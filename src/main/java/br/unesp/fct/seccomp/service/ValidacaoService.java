package br.unesp.fct.seccomp.service;

import org.springframework.stereotype.Service;

@Service
public class ValidacaoService {

    public boolean validarNovaSenha(String senha) {
        if (senha == null || senha.length() < 8) {
            return false;
        }
        boolean temNumero = false;
        boolean temMaiuscula = false;
        for (char c : senha.toCharArray()) {
            if (Character.isDigit(c)) {
                temNumero = true;
            } else if (Character.isUpperCase(c)) {
                temMaiuscula = true;
            }
        }
        return temNumero && temMaiuscula;
    }

    public boolean validarEmailInstitucional(String email) {
        if (email == null) {
            return false;
        }
        String lowerEmail = email.toLowerCase();
        return lowerEmail.endsWith("@unesp.br") || lowerEmail.endsWith(".unesp.br") || lowerEmail.contains("unesp.br");
    }

    private boolean validarSenha() {
        return false;
    }
}
