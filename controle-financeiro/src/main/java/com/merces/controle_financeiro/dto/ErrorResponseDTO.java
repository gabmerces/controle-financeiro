package com.merces.controle_financeiro.dto;

import java.time.LocalDateTime;

public class ErrorResponseDTO {

    private String mensagem;
    private int status;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponseDTO(String mensagem, int status) {
        this.mensagem = mensagem;
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public int getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
