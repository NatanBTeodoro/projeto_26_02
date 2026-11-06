package com.projeto.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.projeto.dto.ImportacaoCardapioDTO;

public final class ImportacaoCardapio {

	private ImportacaoCardapio() {
	}

	public record CategoriaConhecida(String nome, boolean ativa) {
	}

	public record ProdutoConhecido(
			String categoria,
			String nome,
			BigDecimal preco,
			BigDecimal custo,
			boolean ativo) {
	}

	public static ImportacaoCardapioDTO.Previa prever(
			List<LeitorPlanilhaCardapio.Bruta> linhas,
			List<CategoriaConhecida> categorias,
			List<ProdutoConhecido> produtos) {
		Map<String, CategoriaConhecida> porCategoria = new LinkedHashMap<>();
		for (CategoriaConhecida categoria : categorias) {
			porCategoria.putIfAbsent(chave(categoria.nome()), categoria);
		}
		Map<String, ProdutoConhecido> porProduto = new LinkedHashMap<>();
		for (ProdutoConhecido produto : produtos) {
			porProduto.putIfAbsent(chave(produto.categoria()) + "|" + chave(produto.nome()), produto);
		}

		Set<String> vistos = new HashSet<>();
		Set<String> categoriasNovas = new HashSet<>();
		List<ImportacaoCardapioDTO.Linha> saida = new ArrayList<>();
		int criar = 0;
		int atualizar = 0;
		int erros = 0;
		int semCusto = 0;

		for (LeitorPlanilhaCardapio.Bruta bruta : linhas) {
			List<String> mensagens = new ArrayList<>();
			String categoria = bruta.categoria() == null ? "" : bruta.categoria().trim();
			String nome = bruta.nome() == null ? "" : bruta.nome().trim();
			if (categoria.isEmpty()) {
				mensagens.add("Informe a categoria");
			} else if (categoria.length() > 120) {
				mensagens.add("A categoria passa de 120 caracteres");
			}
			if (nome.isEmpty()) {
				mensagens.add("Informe o nome");
			} else if (nome.length() > 150) {
				mensagens.add("O nome passa de 150 caracteres");
			}

			NumerosPlanilha.Leitura preco = NumerosPlanilha.ler(bruta.preco());
			NumerosPlanilha.Leitura custo = NumerosPlanilha.ler(bruta.custo());
			if (preco.vazio()) {
				mensagens.add("Informe o preço");
			} else if (preco.erro() != null) {
				mensagens.add("Preço inválido");
			} else if (preco.valor().signum() < 0) {
				mensagens.add("O preço precisa ser zero ou maior");
			}
			if (custo.erro() != null) {
				mensagens.add("Custo inválido");
			} else if (!custo.vazio() && custo.valor().signum() < 0) {
				mensagens.add("O custo precisa ser zero ou maior");
			}

			String chaveProduto = chave(categoria) + "|" + chave(nome);
			boolean repetido = !categoria.isEmpty() && !nome.isEmpty() && !vistos.add(chaveProduto);
			if (repetido) {
				mensagens.add("Produto repetido neste arquivo");
			}

			boolean categoriaNova = !categoria.isEmpty() && !porCategoria.containsKey(chave(categoria));
			CategoriaConhecida conhecida = porCategoria.get(chave(categoria));
			boolean reativar = conhecida != null && !conhecida.ativa();
			ProdutoConhecido existente = porProduto.get(chaveProduto);
			boolean semCustoLinha = custo.vazio() || custo.valor() == null;
			if (semCustoLinha && mensagens.isEmpty()) {
				mensagens.add("Sem custo: ficará de fora da classificação");
			}
			if (existente != null && !existente.ativo() && mensagens.stream().noneMatch(msg -> msg.startsWith("Produto repetido"))) {
				mensagens.add("O produto está inativo e continua inativo");
			}

			ImportacaoCardapioDTO.Situacao situacao = mensagens.stream().anyMatch(ImportacaoCardapio::ehErro)
					? ImportacaoCardapioDTO.Situacao.ERRO
					: existente == null
							? ImportacaoCardapioDTO.Situacao.CRIAR
							: ImportacaoCardapioDTO.Situacao.ATUALIZAR;
			if (situacao == ImportacaoCardapioDTO.Situacao.ERRO) {
				erros++;
			} else if (situacao == ImportacaoCardapioDTO.Situacao.CRIAR) {
				criar++;
				if (categoriaNova) {
					categoriasNovas.add(chave(categoria));
				}
				if (semCustoLinha) {
					semCusto++;
				}
			} else {
				atualizar++;
				if (semCustoLinha) {
					semCusto++;
				}
			}

			saida.add(new ImportacaoCardapioDTO.Linha(
					bruta.numero(),
					categoria,
					nome,
					preco.erro() == null && !preco.vazio() ? preco.valor() : null,
					custo.erro() == null && !custo.vazio() ? custo.valor() : null,
					existente == null ? null : existente.preco(),
					existente == null ? null : existente.custo(),
					situacao,
					categoriaNova && situacao != ImportacaoCardapioDTO.Situacao.ERRO,
					reativar && situacao != ImportacaoCardapioDTO.Situacao.ERRO,
					semCustoLinha && situacao != ImportacaoCardapioDTO.Situacao.ERRO,
					existente != null && !existente.ativo(),
					List.copyOf(mensagens)));
		}

		return new ImportacaoCardapioDTO.Previa(
				List.copyOf(saida),
				new ImportacaoCardapioDTO.Resumo(criar, atualizar, categoriasNovas.size(), erros, semCusto));
	}

	public static boolean deveAplicar(ImportacaoCardapioDTO.Modo modo, int erros) {
		return modo == ImportacaoCardapioDTO.Modo.SOMENTE_VALIDAS || erros == 0;
	}

	private static boolean ehErro(String mensagem) {
		return !mensagem.startsWith("Sem custo") && !mensagem.startsWith("O produto está inativo");
	}

	private static String chave(String texto) {
		return texto == null ? "" : texto.trim().toLowerCase(Locale.forLanguageTag("pt-BR"));
	}
}
