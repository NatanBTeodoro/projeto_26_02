package com.projeto.util.exception;

public class OperacaoNaoPermitidaException extends RuntimeException {

	public OperacaoNaoPermitidaException(String mensagem) {
		super(mensagem);
	}
}
