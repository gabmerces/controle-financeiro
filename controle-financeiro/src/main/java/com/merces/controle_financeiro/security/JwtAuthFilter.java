package com.merces.controle_financeiro.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Protege as rotas de API que exigem login: exige um header "Authorization: Bearer {token}"
 * válido e disponibiliza o ID do usuário autenticado como atributo da requisição
 * ("usuarioId"), para os controllers nunca mais precisarem confiar em um ID enviado
 * pelo próprio cliente (o que antes permitia um usuário acessar dados de outro só
 * trocando o ID na URL — essa era a falha de IDOR).
 *
 * Registrado explicitamente (ver FilterConfig) apenas para o padrão "/api/*", para não
 * bloquear o carregamento dos arquivos estáticos (HTML/CSS/JS).
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String[] ROTAS_PUBLICAS = {
            "/api/usuarios/cadastrar",
            "/api/usuarios/login",
            "/api/usuarios/solicitar-recuperacao-senha",
            "/api/usuarios/redefinir-senha"
    };

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String caminho = request.getRequestURI();

        // Preflight de CORS e rotas públicas (cadastro/login/recuperação) não exigem token
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isRotaPublica(caminho)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            responderNaoAutorizado(response, "Token de autenticação ausente. Faça login novamente.");
            return;
        }

        Long usuarioId = jwtUtil.validarTokenERetornarUsuarioId(authHeader.substring(7));
        if (usuarioId == null) {
            responderNaoAutorizado(response, "Token inválido ou expirado. Faça login novamente.");
            return;
        }

        request.setAttribute("usuarioId", usuarioId);
        filterChain.doFilter(request, response);
    }

    private boolean isRotaPublica(String caminho) {
        for (String rota : ROTAS_PUBLICAS) {
            if (caminho.equals(rota)) {
                return true;
            }
        }
        return false;
    }

    private void responderNaoAutorizado(HttpServletResponse response, String mensagem) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"mensagem\":\"" + mensagem + "\",\"status\":401}");
    }
}
