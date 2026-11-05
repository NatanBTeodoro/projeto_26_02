package com.projeto.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.dto.PedidoDTO;
import com.projeto.entity.Pedido;
import com.projeto.entity.PedidoItem;
import com.projeto.entity.Produto;
import com.projeto.entity.enums.StatusPedido;
import com.projeto.repository.PedidoRepository;
import com.projeto.repository.ProdutoRepository;
import com.projeto.util.exception.RecursoNaoEncontradoException;
import com.projeto.util.exception.RegraDeNegocioException;

@Service
@Transactional(readOnly = true)
public class PedidoService {

	private final PedidoRepository pedidoRepository;
	private final ProdutoRepository produtoRepository;

	public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository) {
		this.pedidoRepository = pedidoRepository;
		this.produtoRepository = produtoRepository;
	}

	public List<PedidoDTO.Response> listar() {
		return pedidoRepository.findAllByOrderByDataHoraDesc().stream().map(PedidoDTO.Response::de).toList();
	}

	@Transactional
	public PedidoDTO.Response criar(PedidoDTO.Create dto) {
		Instant agora = Instant.now();
		RegrasPedido.validar(dto.dataHora(), dto.itens(), agora);
		Pedido pedido = new Pedido();
		pedido.setDataHora(dto.dataHora() == null ? agora : dto.dataHora());
		pedido.setStatus(StatusPedido.ATIVO);
		for (Map.Entry<Long, Integer> entrada : agrupar(dto.itens()).entrySet()) {
			Produto produto = produtoRepository.findById(entrada.getKey())
					.orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
			if (!produto.isAtivo()) {
				throw new RegraDeNegocioException("Produto inativo não entra em pedido novo");
			}
			PedidoItem linha = new PedidoItem();
			linha.setPedido(pedido);
			linha.setProduto(produto);
			linha.setQuantidade(entrada.getValue());
			linha.setPrecoUnitario(produto.getPrecoAtual());
			linha.setCustoUnitario(produto.getCustoAtual());
			pedido.getItens().add(linha);
		}
		return PedidoDTO.Response.de(pedidoRepository.save(pedido));
	}

	static Map<Long, Integer> agrupar(List<PedidoDTO.ItemCreate> itens) {
		Map<Long, Integer> quantidades = new LinkedHashMap<>();
		for (PedidoDTO.ItemCreate item : itens) {
			quantidades.merge(item.produtoId(), item.quantidade(), Math::addExact);
		}
		return quantidades;
	}

	@Transactional
	public PedidoDTO.Response estornar(Long id, PedidoDTO.Estorno dto) {
		Pedido pedido = pedidoRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
		if (pedido.getStatus() == StatusPedido.ESTORNADO) {
			throw new RegraDeNegocioException("Pedido já estornado");
		}
		pedido.setStatus(StatusPedido.ESTORNADO);
		pedido.setMotivoEstorno(dto.motivo().trim());
		return PedidoDTO.Response.de(pedidoRepository.save(pedido));
	}
}
