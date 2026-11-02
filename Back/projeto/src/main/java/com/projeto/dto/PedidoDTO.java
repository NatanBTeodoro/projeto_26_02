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
			@NotEmpty @Valid List<ItemCreate> itens,
			Long unidadeId,
			@Size(max = 120) String clienteCodigo) {
	}

	public record Estorno(@NotBlank @Size(max = 255) String motivo) {
	}

	public record ItemResponse(
			Long id,
			Long produtoId,
			Integer quantidade,
			BigDecimal precoUnitario,
			BigDecimal custoUnitario) {

		public static ItemResponse from(PedidoItem item, boolean ocultarCusto) {
			return new ItemResponse(
					item.getId(),
					item.getProduto().getId(),
					item.getQuantidade(),
					item.getPrecoUnitario(),
					ocultarCusto ? null : item.getCustoUnitario());
		}
	}

	public record Response(
			Long id,
			Long estabelecimentoId,
			Long unidadeId,
			String externalId,
			Instant dataHora,
			StatusPedido status,
			String motivoEstorno,
			List<ItemResponse> itens) {

		public static Response from(Pedido pedido, boolean ocultarCusto) {
			return new Response(
					pedido.getId(),
					pedido.getEstabelecimento().getId(),
					pedido.getUnidade() != null ? pedido.getUnidade().getId() : null,
					pedido.getExternalId(),
					pedido.getDataHora(),
					pedido.getStatus(),
					pedido.getMotivoEstorno(),
					pedido.getItens().stream().map(item -> ItemResponse.from(item, ocultarCusto)).toList());
		}
	}
}
