package com.projeto.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.projeto.entity.enums.ClasseMenu;

public final class ClassificadorCardapio {

	private static final BigDecimal LIMITE_POPULARIDADE = new BigDecimal("0.70");
	private static final int ESCALA_CONTA = 8;

	private ClassificadorCardapio() {
	}

	public record Linha(long produtoId, String nome, int quantidade, BigDecimal preco, BigDecimal custo) {
	}

	public record Item(
			long produtoId,
			String nome,
			long quantidade,
			BigDecimal popularidade,
			BigDecimal margem,
			ClasseMenu classe) {
	}

	public static List<Item> classificar(List<Linha> linhas) {
		Map<Long, Acumulo> porProduto = new LinkedHashMap<>();
		long total = 0;
		for (Linha linha : linhas) {
			Acumulo acumulo = porProduto.computeIfAbsent(linha.produtoId(), id -> new Acumulo(id, linha.nome()));
			acumulo.somar(linha);
			total += linha.quantidade();
		}
		if (total == 0) {
			return List.of();
		}

		List<Acumulo> elegiveis = porProduto.values().stream().filter(Acumulo::custoCompleto).toList();
		BigDecimal media = mediaMargem(elegiveis);
		BigDecimal limite = elegiveis.isEmpty()
				? null
				: LIMITE_POPULARIDADE.divide(BigDecimal.valueOf(elegiveis.size()), ESCALA_CONTA, RoundingMode.HALF_UP);

		List<Item> itens = new ArrayList<>();
		for (Acumulo acumulo : porProduto.values()) {
			BigDecimal participacao = BigDecimal.valueOf(acumulo.quantidade)
					.divide(BigDecimal.valueOf(total), ESCALA_CONTA, RoundingMode.HALF_UP);
			BigDecimal popularidade = participacao.multiply(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);
			if (!acumulo.custoCompleto()) {
				itens.add(acumulo.item(popularidade, null, ClasseMenu.DADO_INCOMPLETO));
				continue;
			}
			BigDecimal margem = acumulo.margem().setScale(2, RoundingMode.HALF_UP);
			boolean popular = limite != null && participacao.compareTo(limite) >= 0;
			boolean margemAlta = media != null && acumulo.margem().compareTo(media) > 0;
			ClasseMenu classe = classe(popular, margemAlta);
			itens.add(acumulo.item(popularidade, margem, classe));
		}
		itens.sort(Comparator.comparing(Item::nome, String.CASE_INSENSITIVE_ORDER));
		return List.copyOf(itens);
	}

	public static BigDecimal receita(List<Linha> linhas) {
		BigDecimal total = BigDecimal.ZERO;
		for (Linha linha : linhas) {
			total = total.add(linha.preco().multiply(BigDecimal.valueOf(linha.quantidade())));
		}
		return total.setScale(2, RoundingMode.HALF_UP);
	}

	private static ClasseMenu classe(boolean popular, boolean margemAlta) {
		if (popular && margemAlta) {
			return ClasseMenu.ESTRELA;
		}
		if (popular) {
			return ClasseMenu.VACA_LEITEIRA;
		}
		if (margemAlta) {
			return ClasseMenu.ENIGMA;
		}
		return ClasseMenu.CAO;
	}

	private static BigDecimal mediaMargem(List<Acumulo> elegiveis) {
		long quantidade = 0;
		BigDecimal soma = BigDecimal.ZERO;
		for (Acumulo acumulo : elegiveis) {
			quantidade += acumulo.quantidade;
			soma = soma.add(acumulo.somaMargem);
		}
		if (quantidade == 0) {
			return null;
		}
		return soma.divide(BigDecimal.valueOf(quantidade), ESCALA_CONTA, RoundingMode.HALF_UP);
	}

	private static final class Acumulo {
		private final long produtoId;
		private final String nome;
		private long quantidade;
		private BigDecimal somaMargem = BigDecimal.ZERO;
		private boolean custoCompleto = true;

		private Acumulo(long produtoId, String nome) {
			this.produtoId = produtoId;
			this.nome = nome;
		}

		private void somar(Linha linha) {
			quantidade += linha.quantidade();
			if (linha.custo() == null) {
				custoCompleto = false;
				return;
			}
			BigDecimal margem = linha.preco().subtract(linha.custo()).multiply(BigDecimal.valueOf(linha.quantidade()));
			somaMargem = somaMargem.add(margem);
		}

		private boolean custoCompleto() {
			return custoCompleto;
		}

		private BigDecimal margem() {
			return somaMargem.divide(BigDecimal.valueOf(quantidade), ESCALA_CONTA, RoundingMode.HALF_UP);
		}

		private Item item(BigDecimal popularidade, BigDecimal margem, ClasseMenu classe) {
			return new Item(produtoId, nome, quantidade, popularidade, margem, classe);
		}
	}
}
