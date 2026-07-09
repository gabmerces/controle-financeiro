package com.merces.controle_financeiro.exception;

/** Lançada quando o e-mail ou a senha informados no login não conferem. Retorna HTTP 401. */
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
