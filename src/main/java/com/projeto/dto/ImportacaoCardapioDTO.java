package com.projeto.dto;

import java.math.BigDecimal;
import java.util.List;

public final class ImportacaoCardapioDTO {

	private ImportacaoCardapioDTO() {
	}

	public enum Situacao {
		CRIAR,
		ATUALIZAR,
		ERRO
	}

	public enum Modo {
		CANCELAR_TUDO,
		SOMENTE_VALIDAS
	}

	public record Linha(
			int numero,
			String categoria,
			String nome,
			BigDecimal preco,
			BigDecimal custo,
			BigDecimal precoAnterior,
			BigDecimal custoAnterior,
			Situacao situacao,
			boolean categoriaNova,
			boolean reativarCategoria,
			boolean semCusto,
			boolean produtoInativo,
			List<String> mensagens) {
	}

	public record Resumo(int criar, int atualizar, int categoriasNovas, int erros, int semCusto) {
	}

	public record Previa(List<Linha> linhas, Resumo resumo) {
	}

	public record Resultado(boolean aplicado, int gravados, List<Linha> linhas, Resumo resumo) {
	}
}
