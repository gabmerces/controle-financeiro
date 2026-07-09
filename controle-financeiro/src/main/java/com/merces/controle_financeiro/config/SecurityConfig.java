package com.merces.controle_financeiro.config;

import com.merces.controle_financeiro.security.JwtAuthFilter;
import com.merces.controle_financeiro.security.JwtUtil;
import com.merces.controle_financeiro.security.RateLimitFilter;
import com.merces.controle_financeiro.security.SecurityHeadersFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    // BCrypt: gera um hash diferente a cada chamada (por causa do "salt" aleatório),
    // mesmo para a mesma senha. Por isso nunca comparamos senhas com .equals(),
    // e sim com passwordEncoder.matches(senhaDigitada, hashSalvo).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Registra o filtro JWT apenas para as rotas de API, sem afetar os arquivos
    // estáticos (HTML/CSS/JS) servidos pelo Spring.
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtUtil jwtUtil) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtAuthFilter(jwtUtil));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }

    // Protege as rotas de login/cadastro/recuperação de senha contra força bruta.
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(0); // roda antes do JwtAuthFilter
        return registration;
    }

    // Adiciona cabeçalhos de segurança em todas as respostas (não só na API).
    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration() {
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SecurityHeadersFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(2);
        return registration;
    }
}
