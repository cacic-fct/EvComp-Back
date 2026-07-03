package br.unesp.fct.evcomp.service;

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;
import java.util.HashMap;

@Service
public class SistemaEmail {

    @Value("${resend.api.key}")
    private String RESEND_API_KEY; 

    public void enviarEmailRedefinicao(String email, int tokenGerado) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(RESEND_API_KEY);

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; padding: 20px; color: #333;\">" +
                                 "<h2 style=\"color: #0056b3;\">Recuperação de Senha</h2>" +
                                 "<p>Você solicitou a redefinição de senha para sua conta no <strong>EvComp</strong>.</p>" +
                                 "<p>Seu código de segurança é: <strong style=\"font-size: 24px; color: #d9534f;\">" + tokenGerado + "</strong></p>" +
                                 "<p>Ou clique no link abaixo para redefinir diretamente:</p>" +
                                 "<a href=\"http://localhost:3000/redefinir-senha?token=" + tokenGerado + "\" style=\"display: inline-block; padding: 10px 20px; margin-top: 10px; color: #fff; background-color: #0056b3; text-decoration: none; border-radius: 5px;\">Redefinir Senha</a>" +
                                 "<p style=\"margin-top: 20px; font-size: 12px; color: #777;\">Se você não solicitou isso, ignore este e-mail.</p>" +
                                 "</div>";

            Map<String, Object> requestBody = new HashMap<>();

            requestBody.put("from", "EvComp <no-reply@ciriaco.dev>");
            requestBody.put("to", new String[]{email});
            requestBody.put("subject", "Recuperação de Senha - EvComp");
            requestBody.put("html", htmlContent);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            restTemplate.postForEntity("https://api.resend.com/emails", entity, String.class);

        } catch (Exception e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
        }
    }
}
