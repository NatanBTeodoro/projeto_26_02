package com.projeto.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.projeto.service.MotorMenu;
import com.projeto.entity.enums.ClasseMenu;
import com.projeto.entity.enums.CriterioPopularidade;
import com.projeto.entity.enums.Granularidade;

public final class CalculadoraCardapio {

	private CalculadoraCardapio() {
	}

	public static Saida calcular(
			List<ConsultaVendas.Linha> linhas,
			Parametros parametros,
			Map<Long, Instant> primeirasVendas,
			Instant inicio,
			Instant fim,
			Granularidade granularidade) {
		List<Acumulo> acumulos = acumular(linhas);
		boolean periodoCurto = granularidade == Granularidade.DIA
				|| Duration.between(inicio, fim).toDays() < parametros.periodoMinimoDias();
		Instant corteNovo = fim.minus(Duration.ofDays(parametros.periodoMinimoDias()));

		List<ItemClassificado> observacao = new ArrayList<>();
		List<ItemClassificado> incompletos = new ArrayList<>();
		List<ItemClassificado> elegiveis = new ArrayList<>();
		long quantidadeConsiderada = acumulos.stream().mapToLong(Acumulo::quantidade).sum();

		for (Acumulo acumulo : acumulos) {
			BigDecimal popularidade = MotorMenu.popularidadePercentual(acumulo.quantidade(), quantidadeConsiderada);
			BigDecimal margem = acumulo.custoCompleto() ? acumulo.margemUnitaria() : null;
			Instant primeira = primeirasVendas.get(acumulo.produtoId());
			boolean novo = primeira != null && primeira.isAfter(corteNovo);
			ItemClassificado item = base(acumulo, popularidade, margem);
			if (!acumulo.custoCompleto()) {
				incompletos.add(item.comClasse(ClasseMenu.DADO_INCOMPLETO));
			} else if (periodoCurto || novo) {
				observacao.add(item.comClasse(ClasseMenu.EM_OBSERVACAO));
			} else {
				elegiveis.add(item);
			}
		}

		List<ItemClassificado> classificados = new ArrayList<>();
		classificados.addAll(incompletos);
		classificados.addAll(observacao);
		classificados.addAll(classificarElegiveis(elegiveis, parametros));

		List<Set<String>> cestas = cestas(linhas, rotulos(acumulos));
		MotorMenu.ResultadoMineracao mineracao = MotorMenu.minerar(
				cestas,
				parametros.suporteMin(),
				parametros.confiancaMin(),
				parametros.liftMin(),
				true);
		return new Saida(List.copyOf(classificados), mineracao);
	}

	private static List<ItemClassificado> classificarElegiveis(List<ItemClassificado> elegiveis, Parametros parametros) {
		if (elegiveis.isEmpty()) {
			return List.of();
		}
		List<ItemClassificado> saida = new ArrayList<>();
		if (!parametros.escopoPorCategoria()) {
			saida.addAll(classificarGrupo(elegiveis, parametros.criterio()));
			return saida;
		}
		Map<Long, List<ItemClassificado>> porCategoria = new LinkedHashMap<>();
		List<ItemClassificado> geral = new ArrayList<>();
		for (ItemClassificado item : elegiveis) {
			Long chave = item.categoriaId() == null ? -1L : item.categoriaId();
			porCategoria.computeIfAbsent(chave, id -> new ArrayList<>()).add(item);
		}
		for (List<ItemClassificado> grupo : porCategoria.values()) {
			if (grupo.size() < 3) {
				geral.addAll(grupo);
			} else {
				saida.addAll(classificarGrupo(grupo, parametros.criterio()));
			}
		}
		if (!geral.isEmpty()) {
			saida.addAll(classificarGrupo(geral, parametros.criterio()));
		}
		return saida;
	}

	private static List<ItemClassificado> classificarGrupo(List<ItemClassificado> grupo, CriterioPopularidade criterio) {
		long total = grupo.stream().mapToLong(ItemClassificado::quantidade).sum();
		List<BigDecimal> margens = grupo.stream().map(ItemClassificado::margem).toList();
		List<Long> quantidades = grupo.stream().map(ItemClassificado::quantidade).toList();
		BigDecimal media = criterio == CriterioPopularidade.SIMPLES
				? MotorMenu.mediaSimples(margens)
				: MotorMenu.mediaPonderada(margens, quantidades);
		List<ItemClassificado> saida = new ArrayList<>();
		for (ItemClassificado item : grupo) {
			BigDecimal popularidade = MotorMenu.popularidadePercentual(item.quantidade(), total);
			boolean itemPopular = MotorMenu.popular(popularidade, grupo.size());
			boolean alta = MotorMenu.margemAlta(item.margem(), media);
			saida.add(item.comPopularidade(popularidade).comClasse(MotorMenu.classe(itemPopular, alta)));
		}
		return saida;
	}

	private static List<Acumulo> acumular(List<ConsultaVendas.Linha> linhas) {
		Map<Long, Acumulo> mapa = new LinkedHashMap<>();
		for (ConsultaVendas.Linha linha : linhas) {
			if (linha.precoUnitario() == null || linha.precoUnitario().compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}
			mapa.computeIfAbsent(linha.produtoId(), id -> new Acumulo(
					linha.produtoId(),
					linha.produtoNome(),
					linha.categoriaId(),
					linha.categoriaNome())).somar(linha);
		}
		return new ArrayList<>(mapa.values());
	}

	private static Map<Long, String> rotulos(List<Acumulo> acumulos) {
		Map<Long, String> rotulos = new HashMap<>();
		for (Acumulo acumulo : acumulos) {
			rotulos.put(acumulo.produtoId(), acumulo.rotulo());
		}
		return rotulos;
	}

	private static List<Set<String>> cestas(List<ConsultaVendas.Linha> linhas, Map<Long, String> rotulos) {
		Map<Long, Set<String>> porPedido = new LinkedHashMap<>();
		for (ConsultaVendas.Linha linha : linhas) {
			if (linha.precoUnitario() == null || linha.precoUnitario().compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}
			String rotulo = rotulos.get(linha.produtoId());
			if (rotulo == null) {
				continue;
			}
			porPedido.computeIfAbsent(linha.pedidoId(), id -> new HashSet<>()).add(rotulo);
		}
		return porPedido.values().stream().map(Set::copyOf).toList();
	}

	private static ItemClassificado base(Acumulo acumulo, BigDecimal popularidade, BigDecimal margem) {
		return new ItemClassificado(
				acumulo.produtoId(),
				acumulo.nome(),
				acumulo.rotulo(),
				acumulo.categoriaId(),
				acumulo.categoriaNome(),
				acumulo.quantidade(),
				acumulo.receita(),
				popularidade,
				margem,
				null);
	}

	public record Parametros(
			BigDecimal suporteMin,
			BigDecimal confiancaMin,
			BigDecimal liftMin,
			int periodoMinimoDias,
			boolean escopoPorCategoria,
			CriterioPopularidade criterio) {
	}

	public record ItemClassificado(
			Long produtoId,
			String nome,
			String rotulo,
			Long categoriaId,
			String categoriaNome,
			long quantidade,
			BigDecimal receita,
			BigDecimal popularidade,
			BigDecimal margem,
			ClasseMenu classe) {

		ItemClassificado comClasse(ClasseMenu nova) {
			return new ItemClassificado(produtoId, nome, rotulo, categoriaId, categoriaNome, quantidade, receita,
					popularidade, margem, nova);
		}

		ItemClassificado comPopularidade(BigDecimal nova) {
			return new ItemClassificado(produtoId, nome, rotulo, categoriaId, categoriaNome, quantidade, receita, nova,
					margem, classe);
		}
	}

	public record Saida(List<ItemClassificado> itens, MotorMenu.ResultadoMineracao mineracao) {
	}

	private static final class Acumulo {
		private final Long produtoId;
		private final String nome;
		private final Long categoriaId;
		private final String categoriaNome;
		private long quantidade;
		private BigDecimal receita = BigDecimal.ZERO;
		private BigDecimal margemTotal = BigDecimal.ZERO;
		private boolean custoCompleto = true;

		private Acumulo(Long produtoId, String nome, Long categoriaId, String categoriaNome) {
			this.produtoId = produtoId;
			this.nome = nome;
			this.categoriaId = categoriaId;
			this.categoriaNome = categoriaNome;
		}

		private void somar(ConsultaVendas.Linha linha) {
			quantidade += linha.quantidade();
			BigDecimal qtd = BigDecimal.valueOf(linha.quantidade());
			receita = receita.add(linha.precoUnitario().multiply(qtd));
			if (linha.custoUnitario() == null) {
				custoCompleto = false;
				return;
			}
			BigDecimal margem = linha.precoUnitario().subtract(linha.custoUnitario()).multiply(qtd);
			margemTotal = margemTotal.add(margem);
		}

		private Long produtoId() {
			return produtoId;
		}

		private String nome() {
			return nome;
		}

		private Long categoriaId() {
			return categoriaId;
		}

		private String categoriaNome() {
			return categoriaNome;
		}

		private long quantidade() {
			return quantidade;
		}

		private BigDecimal receita() {
			return receita;
		}

		private boolean custoCompleto() {
			return custoCompleto;
		}

		private BigDecimal margemUnitaria() {
			if (quantidade == 0) {
				return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
			}
			return margemTotal.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.HALF_UP);
		}

		private String rotulo() {
			return nome + " (#" + produtoId + ")";
		}
	}
}
