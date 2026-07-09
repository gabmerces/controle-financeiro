package com.merces.controle_financeiro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RedefinirSenhaDTO {

    @NotBlank(message = "Token de redefinição ausente ou inválido")
    private String token;

    // Regra mantida em sincronia com o checklist visual do front-end
    // (password-rules.js): mínimo 8 caracteres, 1 maiúscula, 1 minúscula, 1 número.
    @NotBlank(message = "A nova senha é obrigatória")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "A senha deve ter no mínimo 8 caracteres, com pelo menos 1 letra maiúscula, 1 minúscula e 1 número"
    )
    private String novaSenha;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }
}
