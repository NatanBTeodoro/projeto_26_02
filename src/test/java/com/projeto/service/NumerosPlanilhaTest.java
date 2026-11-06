package com.projeto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class NumerosPlanilhaTest {

	@Test
	void aceitaVirgulaPontoECifrao() {
		assertEquals(new BigDecimal("9.99"), NumerosPlanilha.ler("9,99").valor());
		assertEquals(new BigDecimal("9.99"), NumerosPlanilha.ler("9.99").valor());
		assertEquals(new BigDecimal("9.90"), NumerosPlanilha.ler("R$ 9,90").valor());
		assertEquals(new BigDecimal("1234.56"), NumerosPlanilha.ler("1.234,56").valor());
		assertEquals(new BigDecimal("1234.56"), NumerosPlanilha.ler("1,234.56").valor());
	}

	@Test
	void vazioNaoEErroETextoInvalidoE() {
		assertTrue(NumerosPlanilha.ler("  ").vazio());
		assertNull(NumerosPlanilha.ler("  ").valor());
		assertEquals("Número inválido", NumerosPlanilha.ler("abc").erro());
	}
}
