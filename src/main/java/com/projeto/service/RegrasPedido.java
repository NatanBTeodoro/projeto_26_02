package com.projeto.service;

import java.time.Instant;
import java.util.List;

import com.projeto.dto.PedidoDTO;
import com.projeto.util.exception.RegraDeNegocioException;

public final class RegrasPedido {

	private RegrasPedido() {
	}

	public static void validar(Instant dataHora, List<PedidoDTO.ItemCreate> itens, Instant agora) {
		if (itens == null || itens.isEmpty()) {
			throw new RegraDeNegocioException("O pedido precisa de ao menos um item");
		}
		for (PedidoDTO.ItemCreate item : itens) {
			if (item.quantidade() == null || item.quantidade() <= 0) {
				throw new RegraDeNegocioException("A quantidade precisa ser maior que zero");
			}
		}
		if (dataHora != null && dataHora.isAfter(agora.plusSeconds(120))) {
			throw new RegraDeNegocioException("A data do pedido não pode ser futura");
		}
	}
}
