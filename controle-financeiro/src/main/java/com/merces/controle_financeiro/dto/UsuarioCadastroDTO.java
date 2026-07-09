package com.merces.controle_financeiro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// DTO (Data Transfer Object) = o "molde" dos dados que a API recebe/devolve,
// separado da entidade JPA (Usuario). Isso evita que campos sensíveis, como a senha,
// vazem sem querer nas respostas, e evita que o cliente consiga mandar campos
// que ele não deveria controlar (como um "id" arbitrário).
public class UsuarioCadastroDTO {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    private String email;

    // Regra mantida em sincronia com o checklist visual do front-end
    // (password-rules.js): mínimo 8 caracteres, 1 maiúscula, 1 minúscula, 1 número.
    @NotBlank(message = "A senha é obrigatória")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "A senha deve ter no mínimo 8 caracteres, com pelo menos 1 letra maiúscula, 1 minúscula e 1 número"
    )
    private String senha;

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
