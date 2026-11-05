package com.projeto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.projeto.dto.AnaliseDTO;
import com.projeto.entity.enums.ClasseMenu;

class CsvMatrizTest {

	@Test
	void geraCsvComSeparadorPontoEVirgulaEBom() {
		AnaliseDTO.Response analise = new AnaliseDTO.Response(
				1,
				new BigDecimal("20.00"),
				new BigDecimal("20.00"),
				new BigDecimal("70.0000"),
				new BigDecimal("10.00"),
				List.of(new AnaliseDTO.Item(
						1L,
						"Pizza Calabresa",
						"Pizzas",
						120,
						new BigDecimal("18.5000"),
						new BigDecimal("22.40"),
						ClasseMenu.ESTRELA,
						ClasseMenu.ESTRELA.getRecomendacao())));

		byte[] arquivo = new CsvMatriz().gerar(analise);
		assertEquals((byte) 0xEF, arquivo[0]);
		assertEquals((byte) 0xBB, arquivo[1]);
		assertEquals((byte) 0xBF, arquivo[2]);

		String texto = new String(arquivo, 3, arquivo.length - 3, StandardCharsets.UTF_8);
		assertTrue(texto.startsWith("Produto;Categoria;Quantidade;Popularidade (%);Margem (R$);Classe;Sugestão\n"));
		assertTrue(texto.contains("Pizza Calabresa;Pizzas;120;18,5000;22,40;Estrela;"));
	}

	@Test
	void escapaPontoEVirgulaAspasEFormula() {
		AnaliseDTO.Response analise = new AnaliseDTO.Response(
				0,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				null,
				null,
				List.of(new AnaliseDTO.Item(
						2L,
						"=A;B \"x\"",
						"Bebidas",
						1,
						new BigDecimal("1.0000"),
						null,
						ClasseMenu.DADO_INCOMPLETO,
						ClasseMenu.DADO_INCOMPLETO.getRecomendacao())));

		String texto = new String(new CsvMatriz().gerar(analise), StandardCharsets.UTF_8);
		assertTrue(texto.contains("\"'=A;B \"\"x\"\"\";Bebidas;1;1,0000;;Dado incompleto;"));
	}
}
