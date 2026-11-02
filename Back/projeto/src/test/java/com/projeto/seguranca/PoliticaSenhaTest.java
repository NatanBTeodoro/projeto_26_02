package com.projeto.seguranca;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.projeto.util.exception.RequisicaoInvalidaException;

class PoliticaSenhaTest {

	@Test
	@DisplayName("Senha com letra e número é aceita")
	void senhaValida() {
		assertDoesNotThrow(() -> PoliticaSenha.validar("Admin123"));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "curta1", "semnumero", "12345678", "abcdefgh" })
	@DisplayName("Senha fraca ou fora do tamanho é recusada")
	void senhaInvalida(String senha) {
		assertThrows(RequisicaoInvalidaException.class, () -> PoliticaSenha.validar(senha));
	}
}
