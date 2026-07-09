package com.merces.controle_financeiro.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limitador de tentativas simples (por IP), aplicado só nas rotas sensíveis de autenticação
 * (login, cadastro e recuperação de senha). Sem isso, um atacante poderia tentar milhares de
 * senhas por segundo contra uma conta (força bruta) ou disparar milhares de e-mails de
 * recuperação de senha.
 *
 * Implementação em memória: suficiente para uma instância única (é o cenário típico dos
 * planos gratuitos de hospedagem usados aqui). Se um dia a aplicação rodar em múltiplas
 * instâncias atrás de um load balancer, isso precisaria migrar para um contador compartilhado
 * (ex.: Redis).
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String[] ROTAS_LIMITADAS = {
            "/api/usuarios/login",
            "/api/usuarios/cadastrar",
            "/api/usuarios/solicitar-recuperacao-senha",
            "/api/usuarios/redefinir-senha"
    };

    private static final int LIMITE_TENTATIVAS = 10;
    private static final long JANELA_MS = 5 * 60 * 1000; // 5 minutos

    private final Map<String, Janela> tentativasPorChave = new ConcurrentHashMap<>();

    private static class Janela {
        final AtomicInteger contagem = new AtomicInteger(0);
        volatile long inicioMs = Instant.now().toEpochMilli();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String caminho = request.getRequestURI();

        if (!isRotaLimitada(caminho) || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String chave = obterIpCliente(request) + ":" + caminho;
        Janela janela = tentativasPorChave.computeIfAbsent(chave, k -> new Janela());

        long agora = Instant.now().toEpochMilli();
        if (agora - janela.inicioMs > JANELA_MS) {
            // janela expirou, reinicia a contagem
            janela.inicioMs = agora;
            janela.contagem.set(0);
        }

        if (janela.contagem.incrementAndGet() > LIMITE_TENTATIVAS) {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"mensagem\":\"Muitas tentativas. Aguarde alguns minutos antes de tentar novamente.\",\"status\":429}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRotaLimitada(String caminho) {
        for (String rota : ROTAS_LIMITADAS) {
            if (caminho.equals(rota)) {
                return true;
            }
        }
        return false;
    }

    // Considera o header X-Forwarded-For (presente atrás de proxies/hospedagens como Render)
    // antes de cair para o IP direto da conexão.
    private String obterIpCliente(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
