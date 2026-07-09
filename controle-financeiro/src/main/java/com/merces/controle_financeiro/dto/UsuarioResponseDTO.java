package com.merces.controle_financeiro.dto;

import com.merces.controle_financeiro.model.Usuario;

// DTO de saída: propositalmente NÃO tem o campo "senha".
// É o que garante que o hash da senha nunca vai parar no JSON de resposta,
// não importa o que alguém esqueça de fazer no controller no futuro.
public class UsuarioResponseDTO {

    private Long id;
    private String nome;
    private String email;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
}
