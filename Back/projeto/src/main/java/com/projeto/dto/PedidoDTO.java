package com.projeto.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.projeto.entity.Pedido;
import com.projeto.entity.PedidoItem;
import com.projeto.entity.enums.StatusPedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class PedidoDTO {

	private PedidoDTO() {
	}

	public record ItemCreate(
			@NotNull Long produtoId,
			@NotNull @Min(1) Integer quantidade) {
	}

	public record Create(
			@NotNull Long estabelecimentoId,
			@Size(max = 80) String externalId,
			Instant dataHora,
			@NotEmpty @Valid List<ItemCreate> itens) {
	}

	public record Estorno(@NotBlank @Size(max = 255) String motivo) {
	}

	public record ItemResponse(
			Long id,
			Long produtoId,
			Integer quantidade,
			BigDecimal precoUnitario,
			BigDecimal custoUnitario) {

		public static ItemResponse from(PedidoItem item) {
			return new ItemResponse(
					item.getId(),
					item.getProduto().getId(),
					item.getQuantidade(),
					item.getPrecoUnitario(),
					item.getCustoUnitario());
		}
	}

	public record Response(
			Long id,
			Long estabelecimentoId,
			String externalId,
			Instant dataHora,
			StatusPedido status,
			String motivoEstorno,
			List<ItemResponse> itens) {

		public static Response from(Pedido pedido) {
			return new Response(
					pedido.getId(),
					pedido.getEstabelecimento().getId(),
					pedido.getExternalId(),
					pedido.getDataHora(),
					pedido.getStatus(),
					pedido.getMotivoEstorno(),
					pedido.getItens().stream().map(ItemResponse::from).toList());
		}
	}
}
