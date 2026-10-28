package com.projeto.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.projeto.entity.Produto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ProdutoDTO {

	private ProdutoDTO() {
	}

	public record Create(
			@NotNull Long estabelecimentoId,
			Long categoriaId,
			@NotBlank @Size(max = 150) String nome,
			@NotNull @DecimalMin("0.00") BigDecimal precoAtual,
			BigDecimal custoAtual,
			Boolean sazonal,
			LocalDate janelaIni,
			LocalDate janelaFim) {
	}

	public record Update(
			Long categoriaId,
			@NotBlank @Size(max = 150) String nome,
			@NotNull @DecimalMin("0.00") BigDecimal precoAtual,
			BigDecimal custoAtual,
			Boolean sazonal,
			LocalDate janelaIni,
			LocalDate janelaFim,
			Boolean ativo) {
	}

	public record Response(
			Long id,
			Long estabelecimentoId,
			Long categoriaId,
			String nome,
			BigDecimal precoAtual,
			BigDecimal custoAtual,
			boolean sazonal,
			LocalDate janelaIni,
			LocalDate janelaFim,
			boolean ativo) {

		public static Response from(Produto p) {
			return new Response(
					p.getId(),
					p.getEstabelecimento().getId(),
					p.getCategoria() != null ? p.getCategoria().getId() : null,
					p.getNome(),
					p.getPrecoAtual(),
					p.getCustoAtual(),
					p.isSazonal(),
					p.getJanelaIni(),
					p.getJanelaFim(),
					p.isAtivo());
		}
	}
}
