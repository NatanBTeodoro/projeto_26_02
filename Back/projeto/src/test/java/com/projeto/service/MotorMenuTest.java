package com.projeto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.projeto.entity.enums.ClasseMenu;

class MotorMenuTest {

	@Test
	@DisplayName("Margem é preço menos custo, e custo ausente fica incompleto")
	void margem() {
		assertEquals(new BigDecimal("6.00"), MotorMenu.margem(new BigDecimal("10.00"), new BigDecimal("4.00")));
		assertEquals(null, MotorMenu.margem(new BigDecimal("10.00"), null));
	}

	@Test
	@DisplayName("Popularidade percentual e o corte de 70 por cento de 1/n")
	void popularidade() {
		assertEquals(new BigDecimal("25.000000"), MotorMenu.popularidadePercentual(25, 100));
		assertEquals(new BigDecimal("0.000000"), MotorMenu.popularidadePercentual(5, 0));
		assertEquals(new BigDecimal("7.000000"), MotorMenu.limitePopularidade(10));
		assertTrue(MotorMenu.popular(new BigDecimal("7.000000"), 10));
		assertFalse(MotorMenu.popular(new BigDecimal("6.999999"), 10));
	}

	@Test
	@DisplayName("Classifica os quatro quadrantes e margem igual à média não é alta")
	void classes() {
		assertEquals(ClasseMenu.ESTRELA, MotorMenu.classe(true, true));
		assertEquals(ClasseMenu.VACA_LEITEIRA, MotorMenu.classe(true, false));
		assertEquals(ClasseMenu.ENIGMA, MotorMenu.classe(false, true));
		assertEquals(ClasseMenu.CAO, MotorMenu.classe(false, false));
		assertFalse(MotorMenu.margemAlta(new BigDecimal("5.00"), new BigDecimal("5.00")));
		assertTrue(MotorMenu.margemAlta(new BigDecimal("5.01"), new BigDecimal("5.00")));
	}

	@Test
	@DisplayName("Regra A para B com lift acima de 1 e cesta de um item só no denominador")
	void mineracao() {
		List<Set<String>> cestas = List.of(
				Set.of("A", "B"),
				Set.of("A", "B"),
				Set.of("A"),
				Set.of("C"));
		MotorMenu.ResultadoMineracao resultado = MotorMenu.minerar(
				cestas,
				new BigDecimal("0.02"),
				new BigDecimal("0.30"),
				BigDecimal.ONE,
				false);
		assertFalse(resultado.baseInsuficiente());
		MotorMenu.RegraMinerada regra = resultado.regras().stream()
				.filter(item -> item.antecedente().equals("A") && item.consequente().equals("B"))
				.findFirst()
				.orElseThrow();
		assertEquals(new BigDecimal("0.500000"), regra.suporte());
		assertEquals(new BigDecimal("0.666667"), regra.confianca());
		assertEquals(new BigDecimal("1.333334"), regra.lift());
		assertTrue(regra.fragil());
	}

	@Test
	@DisplayName("Menos de 200 pedidos não gera regra quando a base mínima é exigida")
	void baseInsuficiente() {
		MotorMenu.ResultadoMineracao resultado = MotorMenu.minerar(
				List.of(Set.of("A", "B")),
				new BigDecimal("0.02"),
				new BigDecimal("0.30"),
				BigDecimal.ONE,
				true);
		assertTrue(resultado.baseInsuficiente());
		assertTrue(resultado.regras().isEmpty());
	}

	@Test
	@DisplayName("Alerta de Cão reinicia quando a classe muda, inclusive em observação")
	void alerta() {
		assertEquals(3, MotorMenu.consecutivosCao(List.of(ClasseMenu.CAO, ClasseMenu.CAO, ClasseMenu.CAO, ClasseMenu.ESTRELA)));
		assertEquals(0, MotorMenu.consecutivosCao(List.of(ClasseMenu.EM_OBSERVACAO, ClasseMenu.CAO)));
		assertTrue(MotorMenu.recomendacao(ClasseMenu.CAO, true).contains("cross-sell"));
	}
}
