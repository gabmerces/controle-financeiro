package com.merces.controle_financeiro.exception;

/** Lançada quando um recurso buscado por ID não existe. Retorna HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
