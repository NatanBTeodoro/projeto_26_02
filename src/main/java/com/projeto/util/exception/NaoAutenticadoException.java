package com.projeto.util.exception;

public class NaoAutenticadoException extends RuntimeException {

	public NaoAutenticadoException(String mensagem) {
		super(mensagem);
	}
}
