package com.projeto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.projeto.entity.enums.ClasseMenu;

class ClassificadorCardapioTest {

	@Test
	void classificaQuatroItensPeloExemploManual() {
		List<ClassificadorCardapio.Linha> linhas = List.of(
				linha(1, "A", 40, "20.00", "10.00"),
				linha(2, "B", 40, "11.00", "10.00"),
				linha(3, "C", 10, "30.00", "10.00"),
				linha(4, "D", 10, "11.00", "10.00"));

		List<ClassificadorCardapio.Item> itens = ClassificadorCardapio.classificar(linhas);

		assertEquals(new BigDecimal("1650.00"), ClassificadorCardapio.receita(linhas));
		assertEquals(ClasseMenu.ESTRELA, item(itens, "A").classe());
		assertEquals(new BigDecimal("10.00"), item(itens, "A").margem());
		assertEquals(new BigDecimal("40.0000"), item(itens, "A").popularidade());
		assertEquals(ClasseMenu.VACA_LEITEIRA, item(itens, "B").classe());
		assertEquals(new BigDecimal("1.00"), item(itens, "B").margem());
		assertEquals(ClasseMenu.ENIGMA, item(itens, "C").classe());
		assertEquals(new BigDecimal("20.00"), item(itens, "C").margem());
		assertEquals(ClasseMenu.CAO, item(itens, "D").classe());
	}

	@Test
	void itemSemCustoFicaDeForaDaMedia() {
		List<ClassificadorCardapio.Linha> linhas = List.of(
				linha(1, "A", 40, "20.00", "10.00"),
				linha(2, "B", 40, "11.00", "10.00"),
				linha(3, "C", 10, "30.00", "10.00"),
				linha(4, "D", 10, "11.00", "10.00"),
				linha(5, "E", 10, "8.00", null));

		ClassificadorCardapio.Item incompleto = item(ClassificadorCardapio.classificar(linhas), "E");

		assertEquals(ClasseMenu.DADO_INCOMPLETO, incompleto.classe());
		assertNull(incompleto.margem());
		assertEquals(10, incompleto.quantidade());
		assertEquals(ClasseMenu.ESTRELA, item(ClassificadorCardapio.classificar(linhas), "A").classe());
	}

	private static ClassificadorCardapio.Linha linha(
			long id,
			String nome,
			int quantidade,
			String preco,
			String custo) {
		return new ClassificadorCardapio.Linha(
				id,
				nome,
				quantidade,
				new BigDecimal(preco),
				custo == null ? null : new BigDecimal(custo));
	}

	private static ClassificadorCardapio.Item item(List<ClassificadorCardapio.Item> itens, String nome) {
		return itens.stream().filter(item -> item.nome().equals(nome)).findFirst().orElseThrow();
	}
}
