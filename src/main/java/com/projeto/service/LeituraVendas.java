package com.projeto.service;

import java.util.ArrayList;
import java.util.List;

import com.projeto.entity.Pedido;
import com.projeto.entity.PedidoItem;
import com.projeto.entity.enums.StatusPedido;

public final class LeituraVendas {

	private LeituraVendas() {
	}

	public static List<ClassificadorCardapio.Linha> linhasAtivas(List<Pedido> pedidos) {
		List<ClassificadorCardapio.Linha> linhas = new ArrayList<>();
		for (Pedido pedido : pedidos) {
			if (pedido.getStatus() != StatusPedido.ATIVO) {
				continue;
			}
			for (PedidoItem item : pedido.getItens()) {
				String categoria = item.getProduto().getCategoria() == null
						? ""
						: item.getProduto().getCategoria().getNome();
				linhas.add(new ClassificadorCardapio.Linha(
						item.getProduto().getId(),
						item.getProduto().getNome(),
						item.getQuantidade(),
						item.getPrecoUnitario(),
						item.getCustoUnitario(),
						categoria));
			}
		}
		return linhas;
	}

	public static long contarPedidosAtivos(List<Pedido> pedidos) {
		return pedidos.stream().filter(pedido -> pedido.getStatus() == StatusPedido.ATIVO).count();
	}
}
