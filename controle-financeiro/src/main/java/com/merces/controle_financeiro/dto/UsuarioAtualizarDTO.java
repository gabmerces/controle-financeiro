package com.merces.controle_financeiro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public class UsuarioAtualizarDTO {

    private String nome; // opcional

    @Email(message = "Informe um e-mail válido")
    private String email; // opcional, mas se vier precisa ter formato válido

    // Campo opcional: string vazia (ou nula) significa "manter a senha atual" (ver
    // UsuarioService.atualizarPerfil). Por isso o padrão aceita "" OU uma senha forte —
    // mesma regra do checklist visual do front-end (password-rules.js).
    @Pattern(
            regexp = "^$|^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "A senha deve ter no mínimo 8 caracteres, com pelo menos 1 letra maiúscula, 1 minúscula e 1 número"
    )
    private String senha; // opcional: só é alterada se vier preenchida

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
