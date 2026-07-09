package com.merces.controle_financeiro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Envio de e-mails transacionais (hoje só o link de redefinição de senha), usando a API HTTP
 * da Brevo (https://www.brevo.com), não SMTP.
 *
 * Por quê API HTTP em vez de SMTP: a maioria das hospedagens gratuitas (Render incluso)
 * BLOQUEIA tráfego de saída nas portas usadas por SMTP (25/465/587), como proteção contra
 * spam saindo de contas gratuitas. A API da Brevo funciona por HTTPS normal (porta 443),
 * então não esbarra nesse bloqueio — e tem um plano grátis de 300 e-mails/dia, sem expirar.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.mail.from}")
    private String remetente;

    @Value("${app.mail.from-name:Controle Financeiro}")
    private String remetenteNome;

    @Value("${app.base-url}")
    private String urlBase;

    @Value("${app.mail.brevo-api-key:}")
    private String brevoApiKey;

    // Se false (padrão quando não há chave de API configurada), os e-mails não são enviados
    // de fato — apenas registrados no log. Isso permite rodar o projeto localmente sem precisar
    // configurar um provedor de e-mail de verdade.
    @Value("${app.mail.enabled:false}")
    private boolean mailHabilitado;

    public void enviarLinkRedefinicaoSenha(String destinatario, String nome, String token) {
        String link = urlBase + "/redefinir-senha.html?token=" + token;

        if (!mailHabilitado || brevoApiKey.isBlank()) {
            // Ambiente de desenvolvimento sem provedor de e-mail configurado: só loga o link
            // no console, pra facilitar testar o fluxo sem precisar de conta em nenhum serviço.
            log.info("[DEV] Link de redefinição de senha para {}: {}", destinatario, link);
            return;
        }

        try {
            Map<String, Object> corpo = new LinkedHashMap<>();
            corpo.put("sender", Map.of("name", remetenteNome, "email", remetente));
            corpo.put("to", new Object[] { Map.of("email", destinatario, "name", nome) });
            corpo.put("subject", "Redefinição de senha — Controle Financeiro");
            corpo.put("htmlContent", montarHtmlEmail(nome, link));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_API_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("api-key", brevoApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(corpo)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                // Nunca deixamos uma falha no envio de e-mail quebrar a resposta da API pro
                // usuário (ele já recebeu a mensagem genérica de sucesso) — só registramos
                // no log do servidor pra investigar depois.
                log.error("Falha ao enviar e-mail via Brevo (status {}): {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de redefinição de senha", e);
        }
    }

    private String montarHtmlEmail(String nome, String link) {
        return "<p>Olá, " + escaparHtml(nome) + "!</p>"
                + "<p>Recebemos um pedido para redefinir a senha da sua conta no Controle Financeiro.</p>"
                + "<p><a href=\"" + link + "\">Clique aqui para criar uma nova senha</a> (válido por 30 minutos).</p>"
                + "<p>Se você não pediu essa redefinição, pode ignorar este e-mail com tranquilidade — "
                + "sua senha atual continua funcionando normalmente.</p>";
    }

    private String escaparHtml(String texto) {
        return texto == null ? "" : texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
