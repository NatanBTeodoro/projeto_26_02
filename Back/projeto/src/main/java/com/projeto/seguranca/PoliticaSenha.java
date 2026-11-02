package com.projeto.seguranca;

import com.projeto.util.exception.RequisicaoInvalidaException;

public final class PoliticaSenha {

	private PoliticaSenha() {
	}

	public static void validar(String senha) {
		if (senha == null || senha.length() < 8 || senha.length() > 72) {
			throw new RequisicaoInvalidaException("A senha precisa ter entre 8 e 72 caracteres");
		}
		boolean letra = senha.chars().anyMatch(Character::isLetter);
		boolean numero = senha.chars().anyMatch(Character::isDigit);
		if (!letra || !numero) {
			throw new RequisicaoInvalidaException("A senha precisa de pelo menos uma letra e um número");
		}
	}
}
