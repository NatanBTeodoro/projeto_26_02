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
			@NotNull(message = "Informe o produto") Long produtoId,
			@NotNull(message = "Informe a quantidade") @Min(value = 1, message = "A quantidade precisa ser maior que zero") Integer quantidade) {
	}

	public record Create(
			Instant dataHora,
			@NotEmpty(message = "O pedido precisa de ao menos um item") @Valid List<ItemCreate> itens) {
	}

	public record Estorno(@NotBlank(message = "Informe o motivo do estorno") @Size(max = 255) String motivo) {
	}

	public record ItemResponse(
			Long id,
			Long produtoId,
			String produtoNome,
			Integer quantidade,
			BigDecimal precoUnitario,
			BigDecimal custoUnitario) {

		public static ItemResponse de(PedidoItem item) {
			return new ItemResponse(
					item.getId(),
					item.getProduto().getId(),
					item.getProduto().getNome(),
					item.getQuantidade(),
					item.getPrecoUnitario(),
					item.getCustoUnitario());
		}
	}

	public record Response(
			Long id,
			Instant dataHora,
			StatusPedido status,
			String motivoEstorno,
			List<ItemResponse> itens) {

		public static Response de(Pedido pedido) {
			return new Response(
					pedido.getId(),
					pedido.getDataHora(),
					pedido.getStatus(),
					pedido.getMotivoEstorno(),
					pedido.getItens().stream().map(ItemResponse::de).toList());
		}
	}
}
