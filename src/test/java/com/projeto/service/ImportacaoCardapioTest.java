package com.projeto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import com.projeto.dto.ImportacaoCardapioDTO;

class ImportacaoCardapioTest {

	@Test
	void previaSeparaCriacaoAtualizacaoCategoriaNovaECustoVazio() {
		var linhas = List.of(
				new LeitorPlanilhaCardapio.Bruta(2, "Bebidas", "Suco", "R$ 9,90", ""),
				new LeitorPlanilhaCardapio.Bruta(3, "Pizzas", "Calabresa", "22.00", "10,00"),
				new LeitorPlanilhaCardapio.Bruta(4, "Pizzas", "Calabresa", "30", "12"),
				new LeitorPlanilhaCardapio.Bruta(5, "Pizzas", "", "10", "4"));

		ImportacaoCardapioDTO.Previa previa = ImportacaoCardapio.prever(
				linhas,
				List.of(new ImportacaoCardapio.CategoriaConhecida("Pizzas", true)),
				List.of(new ImportacaoCardapio.ProdutoConhecido(
						"Pizzas", "Calabresa", new BigDecimal("20.00"), new BigDecimal("8.00"), true)));

		assertEquals(ImportacaoCardapioDTO.Situacao.CRIAR, previa.linhas().get(0).situacao());
		assertTrue(previa.linhas().get(0).categoriaNova());
		assertTrue(previa.linhas().get(0).semCusto());
		assertEquals(ImportacaoCardapioDTO.Situacao.ATUALIZAR, previa.linhas().get(1).situacao());
		assertEquals(new BigDecimal("20.00"), previa.linhas().get(1).precoAnterior());
		assertEquals(new BigDecimal("22.00"), previa.linhas().get(1).preco());
		assertEquals(ImportacaoCardapioDTO.Situacao.ERRO, previa.linhas().get(2).situacao());
		assertEquals(ImportacaoCardapioDTO.Situacao.ERRO, previa.linhas().get(3).situacao());
		assertEquals(1, previa.resumo().criar());
		assertEquals(1, previa.resumo().atualizar());
		assertEquals(1, previa.resumo().categoriasNovas());
		assertEquals(2, previa.resumo().erros());
		assertEquals(1, previa.resumo().semCusto());
		assertFalse(ImportacaoCardapio.deveAplicar(ImportacaoCardapioDTO.Modo.CANCELAR_TUDO, previa.resumo().erros()));
		assertTrue(ImportacaoCardapio.deveAplicar(ImportacaoCardapioDTO.Modo.SOMENTE_VALIDAS, previa.resumo().erros()));
	}

	@Test
	void csvComCabecalhoEAspasViraLinhas() {
		String csv = "categoria;nome;preco;custo\n\"Bebidas; gelada\";Suco;9,90;\n";
		LeitorPlanilhaCardapio.Leitura leitura = LeitorPlanilhaCardapio.ler("cardapio.csv", csv.getBytes());

		assertNull(leitura.erro());
		assertEquals(1, leitura.linhas().size());
		assertEquals("Bebidas; gelada", leitura.linhas().get(0).categoria());
		assertEquals("", leitura.linhas().get(0).custo());
	}

	@Test
	void xlsxUsaAMesmaLeitura() throws Exception {
		byte[] arquivo;
		try (XSSFWorkbook pasta = new XSSFWorkbook()) {
			var aba = pasta.createSheet();
			var cabecalho = aba.createRow(0);
			cabecalho.createCell(0).setCellValue("categoria");
			cabecalho.createCell(1).setCellValue("nome");
			cabecalho.createCell(2).setCellValue("preco");
			cabecalho.createCell(3).setCellValue("custo");
			var linha = aba.createRow(1);
			linha.createCell(0).setCellValue("Pizzas");
			linha.createCell(1).setCellValue("Marguerita");
			linha.createCell(2).setCellValue(28.5);
			linha.createCell(3).setCellValue(11);
			var bytes = new java.io.ByteArrayOutputStream();
			pasta.write(bytes);
			arquivo = bytes.toByteArray();
		}

		LeitorPlanilhaCardapio.Leitura leitura = LeitorPlanilhaCardapio.ler("cardapio.xlsx", arquivo);

		assertNull(leitura.erro());
		assertEquals("Pizzas", leitura.linhas().get(0).categoria());
		assertEquals("Marguerita", leitura.linhas().get(0).nome());
		assertEquals(new BigDecimal("28.50"), NumerosPlanilha.ler(leitura.linhas().get(0).preco()).valor());
	}
}
