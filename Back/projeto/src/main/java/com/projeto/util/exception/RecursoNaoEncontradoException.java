package com.projeto.util.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

	public RecursoNaoEncontradoException(String recurso, Object id) {
		super(recurso + " não encontrado: " + id);
	}

	public RecursoNaoEncontradoException(String mensagem) {
		super(mensagem);
	}
}
