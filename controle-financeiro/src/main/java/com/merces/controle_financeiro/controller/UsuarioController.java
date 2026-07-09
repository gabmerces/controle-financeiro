package com.merces.controle_financeiro.controller;

import com.merces.controle_financeiro.dto.LoginResponseDTO;
import com.merces.controle_financeiro.dto.RedefinirSenhaDTO;
import com.merces.controle_financeiro.dto.SolicitarRecuperacaoDTO;
import com.merces.controle_financeiro.dto.UsuarioAtualizarDTO;
import com.merces.controle_financeiro.dto.UsuarioCadastroDTO;
import com.merces.controle_financeiro.dto.UsuarioLoginDTO;
import com.merces.controle_financeiro.dto.UsuarioResponseDTO;
import com.merces.controle_financeiro.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// A lista de domínios com permissão para chamar esta API é restrita e configurada
// centralmente (ver CorsConfig), lida da variável de ambiente APP_ALLOWED_ORIGIN.
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // ROTA PÚBLICA: Cadastro de usuário
    @PostMapping("/cadastrar")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioCadastroDTO dados) {
        return ResponseEntity.ok(usuarioService.cadastrar(dados));
    }

    // ROTA PÚBLICA: Login — devolve um token JWT que deve ser enviado
    // no header "Authorization: Bearer {token}" em todas as chamadas seguintes.
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody UsuarioLoginDTO dados) {
        return ResponseEntity.ok(usuarioService.login(dados));
    }

    // ROTA PÚBLICA (1/2): Solicita a recuperação de senha — envia um e-mail com um link
    // de uso único (token) caso o e-mail exista. A resposta é sempre a mesma genérica,
    // exista ou não o e-mail, para não permitir que alguém descubra e-mails cadastrados.
    @PostMapping("/solicitar-recuperacao-senha")
    public ResponseEntity<Map<String, String>> solicitarRecuperacaoSenha(@Valid @RequestBody SolicitarRecuperacaoDTO dados) {
        usuarioService.solicitarRecuperacaoSenha(dados);
        return ResponseEntity.ok(Map.of(
                "mensagem", "Se este e-mail estiver cadastrado, enviamos um link de redefinição para ele."
        ));
    }

    // ROTA PÚBLICA (2/2): Confirma a redefinição de senha usando o token recebido por e-mail.
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(@Valid @RequestBody RedefinirSenhaDTO dados) {
        usuarioService.redefinirSenhaComToken(dados);
        return ResponseEntity.ok(Map.of("mensagem", "Sua senha foi redefinida com sucesso!"));
    }

    // ROTA PROTEGIDA: Atualizar o próprio perfil.
    // O ID do usuário vem do token JWT (via JwtAuthFilter), não é mais recebido na URL —
    // isso evita que alguém edite o perfil de outra pessoa só trocando o ID.
    @PutMapping("/atualizar-perfil")
    public ResponseEntity<UsuarioResponseDTO> atualizarPerfil(@Valid @RequestBody UsuarioAtualizarDTO dados,
                                                                HttpServletRequest request) {
        Long usuarioId = (Long) request.getAttribute("usuarioId");
        return ResponseEntity.ok(usuarioService.atualizarPerfil(usuarioId, dados));
    }
}
