package com.merces.controle_financeiro.repository;

import com.merces.controle_financeiro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Esta função vai nos ajudar a procurar o usuário pelo e-mail quando ele tentar fazer login
    Optional<Usuario> findByEmail(String email);

    // Usado na 2ª etapa da recuperação de senha, para localizar o usuário dono do token
    // enviado por e-mail (comparando sempre o hash, nunca o valor em texto puro).
    Optional<Usuario> findByResetTokenHash(String resetTokenHash);
}