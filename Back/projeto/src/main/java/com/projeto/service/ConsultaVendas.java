package com.projeto.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.repository.PedidoItemRepository;
import com.projeto.util.exception.RequisicaoInvalidaException;

@Service
@Transactional(readOnly = true)
public class ConsultaVendas {

	private final PedidoItemRepository pedidoItemRepository;

	public ConsultaVendas(PedidoItemRepository pedidoItemRepository) {
		this.pedidoItemRepository = pedidoItemRepository;
	}

	public List<Linha> linhas(Long estabelecimentoId, Instant inicio, Instant fim) {
		if (inicio == null || fim == null || fim.isBefore(inicio)) {
			throw new RequisicaoInvalidaException("Período inválido");
		}
		List<Linha> linhas = new ArrayList<>();
		for (var item : pedidoItemRepository.buscarAtivosNoPeriodo(estabelecimentoId, inicio, fim)) {
			var pedido = item.getPedido();
			var produto = item.getProduto();
			linhas.add(new Linha(
					pedido.getId(),
					pedido.getDataHora(),
					pedido.getUnidade() != null ? pedido.getUnidade().getId() : null,
					pedido.getClienteHash(),
					produto.getId(),
					produto.getNome(),
					produto.getCategoria() != null ? produto.getCategoria().getId() : null,
					produto.getCategoria() != null ? produto.getCategoria().getNome() : "Sem categoria",
					item.getQuantidade(),
					item.getPrecoUnitario(),
					item.getCustoUnitario()));
		}
		return linhas;
	}

	public Map<Long, Instant> primeirasVendas(Long estabelecimentoId) {
		Map<Long, Instant> primeiras = new HashMap<>();
		for (Object[] linha : pedidoItemRepository.primeiraVendaPorProduto(estabelecimentoId)) {
			primeiras.put((Long) linha[0], (Instant) linha[1]);
		}
		return primeiras;
	}

	public record Linha(
			Long pedidoId,
			Instant dataHora,
			Long unidadeId,
			String clienteHash,
			Long produtoId,
			String produtoNome,
			Long categoriaId,
			String categoriaNome,
			int quantidade,
			BigDecimal precoUnitario,
			BigDecimal custoUnitario) {
	}

	public static Map<Long, List<Linha>> porProduto(List<Linha> linhas) {
		Map<Long, List<Linha>> grupos = new LinkedHashMap<>();
		for (Linha linha : linhas) {
			grupos.computeIfAbsent(linha.produtoId(), id -> new ArrayList<>()).add(linha);
		}
		return grupos;
	}
}
