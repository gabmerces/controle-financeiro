package com.merces.controle_financeiro.controller;

import com.merces.controle_financeiro.dto.TransacaoDTO;
import com.merces.controle_financeiro.dto.TransacaoResponseDTO;
import com.merces.controle_financeiro.service.TransacaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Todas as rotas aqui são protegidas pelo JwtAuthFilter (registrado para "/api/*").
// O ID do usuário logado é sempre lido do token (request.getAttribute("usuarioId")),
// nunca de um valor enviado pelo cliente — é isso que impede um usuário de ver,
// editar ou apagar transações de outro usuário (correção do IDOR).
@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    @Autowired
    private TransacaoService transacaoService;

    // Lista as transações do usuário autenticado (não recebe mais o ID na URL)
    @GetMapping
    public List<TransacaoResponseDTO> listar(HttpServletRequest request) {
        Long usuarioId = (Long) request.getAttribute("usuarioId");
        return transacaoService.listarPorUsuario(usuarioId);
    }

    // Salva uma nova transação vinculada ao usuário autenticado
    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> salvar(@Valid @RequestBody TransacaoDTO dados, HttpServletRequest request) {
        Long usuarioId = (Long) request.getAttribute("usuarioId");
        return ResponseEntity.ok(transacaoService.salvar(dados, usuarioId));
    }

    // Atualiza uma transação existente — só se pertencer ao usuário autenticado
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody TransacaoDTO dados, HttpServletRequest request) {
        Long usuarioId = (Long) request.getAttribute("usuarioId");
        return transacaoService.atualizar(id, dados, usuarioId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensagem", "Transação não encontrada.", "status", 404)));
    }

    // Remove uma transação — só se pertencer ao usuário autenticado
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id, HttpServletRequest request) {
        Long usuarioId = (Long) request.getAttribute("usuarioId");
        boolean deletado = transacaoService.deletar(id, usuarioId);

        if (!deletado) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensagem", "Transação não encontrada.", "status", 404));
        }
        return ResponseEntity.noContent().build();
    }
}
