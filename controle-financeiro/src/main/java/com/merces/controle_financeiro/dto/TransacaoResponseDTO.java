package com.merces.controle_financeiro.dto;

import com.merces.controle_financeiro.model.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransacaoResponseDTO {

    private Long id;
    private String descricao;
    private BigDecimal valor;
    private String tipo;
    private LocalDate data;

    public TransacaoResponseDTO(Transacao transacao) {
        this.id = transacao.getId();
        this.descricao = transacao.getDescricao();
        this.valor = transacao.getValor();
        this.tipo = transacao.getTipo();
        this.data = transacao.getData();
    }

    public Long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDate getData() {
        return data;
    }
}
