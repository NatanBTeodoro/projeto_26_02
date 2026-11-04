package com.projeto.dto;

import java.math.BigDecimal;

import com.projeto.entity.Produto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ProdutoDTO {

	private ProdutoDTO() {
	}

	public record Create(
			@NotNull(message = "Informe a categoria") Long categoriaId,
			@NotBlank(message = "Informe o nome") @Size(max = 150) String nome,
			@NotNull(message = "Informe o preço") @DecimalMin(value = "0.00", message = "O preço precisa ser zero ou maior") BigDecimal precoAtual,
			@DecimalMin(value = "0.00", message = "O custo precisa ser zero ou maior") BigDecimal custoAtual) {
	}

	public record Update(
			@NotNull(message = "Informe a categoria") Long categoriaId,
			@NotBlank(message = "Informe o nome") @Size(max = 150) String nome,
			@NotNull(message = "Informe o preço") @DecimalMin(value = "0.00", message = "O preço precisa ser zero ou maior") BigDecimal precoAtual,
			@DecimalMin(value = "0.00", message = "O custo precisa ser zero ou maior") BigDecimal custoAtual,
			Boolean ativo) {
	}

	public record Response(
			Long id,
			Long categoriaId,
			String categoriaNome,
			String nome,
			BigDecimal precoAtual,
			BigDecimal custoAtual,
			boolean ativo) {

		public static Response de(Produto produto) {
			return new Response(
					produto.getId(),
					produto.getCategoria().getId(),
					produto.getCategoria().getNome(),
					produto.getNome(),
					produto.getPrecoAtual(),
					produto.getCustoAtual(),
					produto.isAtivo());
		}
	}
}
