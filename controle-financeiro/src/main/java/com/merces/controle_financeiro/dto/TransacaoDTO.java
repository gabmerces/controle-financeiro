package com.merces.controle_financeiro.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

// Repare que este DTO NÃO tem um campo "usuario": o dono da transação é sempre
// determinado pelo token JWT no back-end, nunca pelo que o cliente manda no corpo
// da requisição. Isso fecha a brecha de IDOR que existia antes.
public class TransacaoDTO {

    @NotBlank(message = "A descrição é obrigatória")
    private String descricao;

    @NotNull(message = "O valor é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    private BigDecimal valor;

    @NotBlank(message = "O tipo é obrigatório")
    @Pattern(regexp = "RECEITA|DESPESA", message = "O tipo deve ser RECEITA ou DESPESA")
    private String tipo;

    @NotNull(message = "A data é obrigatória")
    private LocalDate data;

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }
}
