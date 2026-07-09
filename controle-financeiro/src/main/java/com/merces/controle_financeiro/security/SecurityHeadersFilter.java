package com.merces.controle_financeiro.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adiciona cabeçalhos HTTP de segurança padrão em todas as respostas. São proteções do
 * navegador do lado do cliente contra ataques comuns (clickjacking, sniffing de MIME type,
 * downgrade para HTTP), que não custam nada e não afetam o funcionamento normal do site.
 */
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Impede que o site seja carregado dentro de um <iframe> de outro domínio (clickjacking)
        response.setHeader("X-Frame-Options", "DENY");

        // Impede que o navegador tente "adivinhar" o tipo de um arquivo diferente do declarado
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Não envia a URL completa como referrer para sites externos
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Força HTTPS por 1 ano em navegadores que já visitaram o site (só tem efeito real
        // quando a aplicação já está sendo servida via HTTPS, o que a hospedagem garante)
        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }

        filterChain.doFilter(request, response);
    }
}
