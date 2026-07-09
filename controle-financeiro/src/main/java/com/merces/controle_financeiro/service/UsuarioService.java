package com.merces.controle_financeiro.service;

import com.merces.controle_financeiro.dto.LoginResponseDTO;
import com.merces.controle_financeiro.dto.RedefinirSenhaDTO;
import com.merces.controle_financeiro.dto.SolicitarRecuperacaoDTO;
import com.merces.controle_financeiro.dto.UsuarioAtualizarDTO;
import com.merces.controle_financeiro.dto.UsuarioCadastroDTO;
import com.merces.controle_financeiro.dto.UsuarioLoginDTO;
import com.merces.controle_financeiro.dto.UsuarioResponseDTO;
import com.merces.controle_financeiro.exception.CredenciaisInvalidasException;
import com.merces.controle_financeiro.exception.RecursoNaoEncontradoException;
import com.merces.controle_financeiro.exception.RegraDeNegocioException;
import com.merces.controle_financeiro.model.Usuario;
import com.merces.controle_financeiro.repository.UsuarioRepository;
import com.merces.controle_financeiro.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class UsuarioService {

    private static final int TOKEN_VALIDADE_MINUTOS = 30;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public UsuarioResponseDTO cadastrar(UsuarioCadastroDTO dados) {
        if (usuarioRepository.findByEmail(dados.getEmail()).isPresent()) {
            throw new RegraDeNegocioException("Este e-mail já está cadastrado!");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dados.getNome());
        usuario.setEmail(dados.getEmail());
        // Nunca salvamos a senha em texto puro: aplicamos o hash BCrypt antes de persistir.
        usuario.setSenha(passwordEncoder.encode(dados.getSenha()));

        return new UsuarioResponseDTO(usuarioRepository.save(usuario));
    }

    public LoginResponseDTO login(UsuarioLoginDTO dados) {
        Usuario usuario = usuarioRepository.findByEmail(dados.getEmail())
                // matches() compara a senha digitada com o hash salvo (nunca .equals() em senha)
                .filter(u -> passwordEncoder.matches(dados.getSenha(), u.getSenha()))
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha incorretos!"));

        String token = jwtUtil.gerarToken(usuario.getId(), usuario.getEmail());
        return new LoginResponseDTO(token, new UsuarioResponseDTO(usuario));
    }

    /**
     * Primeira etapa da recuperação de senha: gera um token de uso único, válido por
     * {@link #TOKEN_VALIDADE_MINUTOS} minutos, e envia por e-mail um link para redefinir a senha.
     *
     * Importante: NUNCA revela se o e-mail existe ou não no sistema (sempre "sucede"
     * silenciosamente do ponto de vista do chamador). Isso evita que alguém use esse
     * endpoint para descobrir quais e-mails têm conta cadastrada (enumeração de usuários).
     */
    public void solicitarRecuperacaoSenha(SolicitarRecuperacaoDTO dados) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(dados.getEmail());
        if (usuarioOpt.isEmpty()) {
            return; // silencioso de propósito — não revela se o e-mail existe
        }

        Usuario usuario = usuarioOpt.get();

        String tokenBruto = gerarTokenAleatorio();
        usuario.setResetTokenHash(hashToken(tokenBruto));
        usuario.setResetTokenExpiracao(LocalDateTime.now().plusMinutes(TOKEN_VALIDADE_MINUTOS));
        usuarioRepository.save(usuario);

        emailService.enviarLinkRedefinicaoSenha(usuario.getEmail(), usuario.getNome(), tokenBruto);
    }

    /**
     * Segunda etapa: valida o token recebido por e-mail (comparando o hash, nunca o texto puro)
     * e a expiração, então efetivamente troca a senha. O token é de uso único: é apagado
     * assim que utilizado, mesmo em caso de erro de outra natureza depois.
     */
    public void redefinirSenhaComToken(RedefinirSenhaDTO dados) {
        String tokenHash = hashToken(dados.getToken());

        Usuario usuario = usuarioRepository.findByResetTokenHash(tokenHash)
                .orElseThrow(() -> new RegraDeNegocioException("Link de redefinição inválido ou já utilizado."));

        if (usuario.getResetTokenExpiracao() == null
                || usuario.getResetTokenExpiracao().isBefore(LocalDateTime.now())) {
            throw new RegraDeNegocioException("Este link de redefinição expirou. Solicite um novo.");
        }

        usuario.setSenha(passwordEncoder.encode(dados.getNovaSenha()));
        usuario.setResetTokenHash(null);
        usuario.setResetTokenExpiracao(null);
        usuarioRepository.save(usuario);
    }

    private String gerarTokenAleatorio() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String tokenBruto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(tokenBruto.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao processar token de redefinição", e);
        }
    }

    // O ID do usuário vem sempre do token JWT (parâmetro usuarioId), nunca de um valor
    // enviado pelo cliente — assim ninguém consegue editar o perfil de outra pessoa.
    public UsuarioResponseDTO atualizarPerfil(Long usuarioId, UsuarioAtualizarDTO dados) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        if (dados.getNome() != null && !dados.getNome().isBlank()) {
            usuario.setNome(dados.getNome());
        }

        if (dados.getEmail() != null && !dados.getEmail().isBlank()) {
            usuarioRepository.findByEmail(dados.getEmail()).ifPresent(outro -> {
                if (!outro.getId().equals(usuarioId)) {
                    throw new RegraDeNegocioException("Este e-mail já está sendo usado por outro usuário!");
                }
            });
            usuario.setEmail(dados.getEmail());
        }

        if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dados.getSenha()));
        }

        return new UsuarioResponseDTO(usuarioRepository.save(usuario));
    }
}
