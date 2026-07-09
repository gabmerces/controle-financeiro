package com.merces.controle_financeiro.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gera e valida tokens no formato JWT (header.payload.assinatura), assinados com HMAC-SHA256.
 * Implementado com JDK puro (javax.crypto) + Jackson (já presente via spring-boot-starter-web),
 * sem depender de bibliotecas externas de JWT.
 */
@Component
public class JwtUtil {

    private static final String ALGORITMO = "HmacSHA256";

    @Value("${jwt.secret}")
    private String segredo;

    @Value("${jwt.expiration-ms}")
    private long expiracaoMs;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String gerarToken(Long usuarioId, String email) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            long agoraSegundos = System.currentTimeMillis() / 1000;
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", usuarioId);
            payload.put("email", email);
            payload.put("iat", agoraSegundos);
            payload.put("exp", agoraSegundos + (expiracaoMs / 1000));

            String headerCodificado = base64UrlEncode(objectMapper.writeValueAsBytes(header));
            String payloadCodificado = base64UrlEncode(objectMapper.writeValueAsBytes(payload));
            String dadosParaAssinar = headerCodificado + "." + payloadCodificado;

            return dadosParaAssinar + "." + assinar(dadosParaAssinar);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token JWT", e);
        }
    }

    /**
     * Valida a assinatura e a expiração do token.
     * Retorna o ID do usuário se o token for válido, ou null caso contrário.
     */
    public Long validarTokenERetornarUsuarioId(String token) {
        try {
            String[] partes = token.split("\\.");
            if (partes.length != 3) {
                return null;
            }

            String dadosAssinados = partes[0] + "." + partes[1];
            String assinaturaEsperada = assinar(dadosAssinados);

            if (!assinaturasIguais(assinaturaEsperada, partes[2])) {
                return null; // token adulterado ou assinado com outro segredo
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(base64UrlDecode(partes[1]), Map.class);

            long expiracao = ((Number) payload.get("exp")).longValue();
            if (System.currentTimeMillis() / 1000 > expiracao) {
                return null; // token expirado
            }

            return ((Number) payload.get("sub")).longValue();
        } catch (Exception e) {
            return null; // qualquer token malformado é tratado como inválido
        }
    }

    private String assinar(String dados) throws Exception {
        Mac mac = Mac.getInstance(ALGORITMO);
        mac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), ALGORITMO));
        return base64UrlEncode(mac.doFinal(dados.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64UrlEncode(byte[] dados) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(dados);
    }

    private byte[] base64UrlDecode(String texto) {
        return Base64.getUrlDecoder().decode(texto);
    }

    // Comparação em tempo constante, para não vazar informação por timing attack
    private boolean assinaturasIguais(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }
}
