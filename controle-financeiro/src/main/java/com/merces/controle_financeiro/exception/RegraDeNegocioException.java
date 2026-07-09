package com.merces.controle_financeiro.exception;

/** Lançada quando uma regra de negócio é violada (ex.: e-mail já cadastrado). Retorna HTTP 400. */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
