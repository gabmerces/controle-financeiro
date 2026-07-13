package com.merces.controle_financeiro.service;

import com.merces.controle_financeiro.dto.TransacaoDTO;
import com.merces.controle_financeiro.dto.TransacaoResponseDTO;
import com.merces.controle_financeiro.exception.RecursoNaoEncontradoException;
import com.merces.controle_financeiro.model.Transacao;
import com.merces.controle_financeiro.model.Usuario;
import com.merces.controle_financeiro.repository.TransacaoRepository;
import com.merces.controle_financeiro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<TransacaoResponseDTO> listarPorUsuario(Long usuarioId) {
        return transacaoRepository.findByUsuarioIdOrderByDataAsc(usuarioId).stream()
                .map(TransacaoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public TransacaoResponseDTO salvar(TransacaoDTO dados, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        Transacao transacao = new Transacao();
        transacao.setDescricao(dados.getDescricao());
        transacao.setValor(dados.getValor());
        transacao.setTipo(dados.getTipo());
        transacao.setData(dados.getData());
        transacao.setUsuario(usuario);

        return new TransacaoResponseDTO(transacaoRepository.save(transacao));
    }

    // Retorna Optional.empty() tanto se a transação não existe quanto se ela pertence
    // a outro usuário — o controller trata os dois casos como "não autorizado", sem
    // revelar qual dos dois motivos foi (evita vazar se um ID existe ou não).
    public Optional<TransacaoResponseDTO> atualizar(Long id, TransacaoDTO dados, Long usuarioId) {
        Optional<Transacao> transacaoExistente = transacaoRepository.findById(id);
        if (transacaoExistente.isEmpty()) {
            return Optional.empty();
        }

        Transacao transacao = transacaoExistente.get();
        if (!transacao.getUsuario().getId().equals(usuarioId)) {
            return Optional.empty(); // pertence a outro usuário: tratado como não encontrada
        }

        transacao.setDescricao(dados.getDescricao());
        transacao.setValor(dados.getValor());
        transacao.setTipo(dados.getTipo());
        transacao.setData(dados.getData());

        return Optional.of(new TransacaoResponseDTO(transacaoRepository.save(transacao)));
    }

    public boolean deletar(Long id, Long usuarioId) {
        Optional<Transacao> transacaoExistente = transacaoRepository.findById(id);
        if (transacaoExistente.isEmpty()) {
            return false;
        }

        if (!transacaoExistente.get().getUsuario().getId().equals(usuarioId)) {
            return false; // pertence a outro usuário
        }

        transacaoRepository.deleteById(id);
        return true;
    }
}