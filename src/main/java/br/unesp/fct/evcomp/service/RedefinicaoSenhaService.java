package br.unesp.fct.evcomp.service;

import org.springframework.stereotype.Service;

@Service
public class RedefinicaoSenhaService {

    /**
     * Valida se a senha atende aos critérios mínimos de segurança:
     * - Pelo menos 8 caracteres
     * - Pelo menos uma letra maiúscula
     * - Pelo menos um número
     */
    public boolean validarSenha(String senha) {
        if (senha == null || senha.length() < 8) {
            return false;
        }
        
        boolean temMaiuscula = false;
        boolean temNumero = false;
        
        for (char c : senha.toCharArray()) {
            if (Character.isUpperCase(c)) {
                temMaiuscula = true;
            }
            if (Character.isDigit(c)) {
                temNumero = true;
            }
            
            // Se já achou os dois, pode parar a iteração para otimizar
            if (temMaiuscula && temNumero) {
                break;
            }
        }
        
        return temMaiuscula && temNumero;
    }
}
