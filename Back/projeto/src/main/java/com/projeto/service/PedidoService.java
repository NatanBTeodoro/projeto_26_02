package com.projeto.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.service.AuditoriaService;
import com.projeto.dto.PedidoDTO;
import com.projeto.entity.Estabelecimento;
import com.projeto.entity.Pedido;
import com.projeto.entity.PedidoItem;
import com.projeto.entity.Produto;
import com.projeto.entity.Unidade;
import com.projeto.entity.enums.Perfil;
import com.projeto.entity.enums.StatusPedido;
import com.projeto.repository.EstabelecimentoRepository;
import com.projeto.repository.PedidoRepository;
import com.projeto.repository.ProdutoRepository;
import com.projeto.repository.UnidadeRepository;
import com.projeto.seguranca.Acesso;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.util.Pseudonimo;
import com.projeto.util.exception.RecursoJaExistenteException;
import com.projeto.util.exception.RecursoNaoEncontradoException;
import com.projeto.util.exception.RegraDeNegocioException;
import com.projeto.util.exception.RequisicaoInvalidaException;

@Service
@Transactional(readOnly = true)
public class PedidoService {

	private final PedidoRepository pedidoRepository;
	private final ProdutoRepository produtoRepository;
	private final EstabelecimentoRepository estabelecimentoRepository;
	private final UnidadeRepository unidadeRepository;
	private final AuditoriaService auditoriaService;
	private final String pepper;

	public PedidoService(
			PedidoRepository pedidoRepository,
			ProdutoRepository produtoRepository,
			EstabelecimentoRepository estabelecimentoRepository,
			UnidadeRepository unidadeRepository,
			AuditoriaService auditoriaService,
			@Value("${app.pepper:quadrante-dev-pepper}") String pepper) {
		this.pedidoRepository = pedidoRepository;
		this.produtoRepository = produtoRepository;
		this.estabelecimentoRepository = estabelecimentoRepository;
		this.unidadeRepository = unidadeRepository;
		this.auditoriaService = auditoriaService;
		this.pepper = pepper;
	}

	public Page<PedidoDTO.Response> listar(Pageable pageable, UsuarioAtual sessao) {
		return pedidoRepository.findByEstabelecimentoId(sessao.estabelecimentoId(), pageable)
				.map(pedido -> PedidoDTO.Response.from(pedido, ocultarCusto(sessao)));
	}

	public PedidoDTO.Response buscar(Long id, UsuarioAtual sessao) {
		return PedidoDTO.Response.from(carregar(id, sessao), ocultarCusto(sessao));
	}

	@Transactional
	public PedidoDTO.Response criar(PedidoDTO.Create dto, UsuarioAtual sessao) {
		Acesso.exigirVendas(sessao);
		Acesso.exigirMesmoEstabelecimento(dto.estabelecimentoId(), sessao);
		if (dto.itens() == null || dto.itens().isEmpty()) {
			throw new RequisicaoInvalidaException("O pedido precisa de ao menos um item");
		}
		Instant data = dto.dataHora() == null ? Instant.now() : dto.dataHora();
		if (data.isAfter(Instant.now().plusSeconds(60))) {
			throw new RequisicaoInvalidaException("A data da venda não pode ser futura");
		}
		String externalId = dto.externalId() == null || dto.externalId().isBlank() ? null : dto.externalId().trim();
		if (externalId != null && pedidoRepository.existsByEstabelecimentoIdAndExternalId(sessao.estabelecimentoId(), externalId)) {
			throw new RecursoJaExistenteException("Identificador externo já usado neste estabelecimento");
		}
		Estabelecimento estabelecimento = estabelecimentoRepository.findById(sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento", sessao.estabelecimentoId()));
		Pedido pedido = new Pedido();
		pedido.setEstabelecimento(estabelecimento);
		pedido.setExternalId(externalId);
		pedido.setDataHora(data);
		pedido.setStatus(StatusPedido.ATIVO);
		pedido.setClienteHash(Pseudonimo.hmac(dto.clienteCodigo(), pepper));
		if (dto.unidadeId() != null) {
			Unidade unidade = unidadeRepository.findByIdAndEstabelecimentoId(dto.unidadeId(), sessao.estabelecimentoId())
					.orElseThrow(() -> new RecursoNaoEncontradoException("Unidade", dto.unidadeId()));
			pedido.setUnidade(unidade);
		}
		Set<Long> produtos = new HashSet<>();
		for (PedidoDTO.ItemCreate item : dto.itens()) {
			if (!produtos.add(item.produtoId())) {
				throw new RequisicaoInvalidaException("Produto repetido no mesmo pedido");
			}
			if (item.quantidade() == null || item.quantidade() < 1) {
				throw new RequisicaoInvalidaException("Quantidade precisa ser um inteiro maior que zero");
			}
			Produto produto = produtoRepository.findByIdAndEstabelecimentoId(item.produtoId(), sessao.estabelecimentoId())
					.orElseThrow(() -> new RecursoNaoEncontradoException("Produto", item.produtoId()));
			if (!produto.isAtivo()) {
				throw new RegraDeNegocioException("Produto inativo não entra em pedido novo");
			}
			PedidoItem linha = new PedidoItem();
			linha.setPedido(pedido);
			linha.setProduto(produto);
			linha.setQuantidade(item.quantidade());
			linha.setPrecoUnitario(produto.getPrecoAtual());
			linha.setCustoUnitario(produto.getCustoAtual());
			pedido.getItens().add(linha);
		}
		Pedido salvo = pedidoRepository.save(pedido);
		auditoriaService.registrar(sessao, "CRIAR", "pedido", salvo.getId(), null, salvo.getStatus().name());
		return PedidoDTO.Response.from(salvo, ocultarCusto(sessao));
	}

	@Transactional
	public PedidoDTO.Response estornar(Long id, PedidoDTO.Estorno dto, UsuarioAtual sessao) {
		Acesso.exigirVendas(sessao);
		Pedido pedido = carregar(id, sessao);
		if (pedido.getStatus() == StatusPedido.ESTORNADO) {
			throw new RegraDeNegocioException("Pedido já estornado");
		}
		pedido.setStatus(StatusPedido.ESTORNADO);
		pedido.setMotivoEstorno(dto.motivo().trim());
		auditoriaService.registrar(sessao, "ESTORNAR", "pedido", pedido.getId(), StatusPedido.ATIVO.name(), dto.motivo().trim());
		return PedidoDTO.Response.from(pedidoRepository.save(pedido), ocultarCusto(sessao));
	}

	private Pedido carregar(Long id, UsuarioAtual sessao) {
		return pedidoRepository.findByIdAndEstabelecimentoId(id, sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
	}

	private boolean ocultarCusto(UsuarioAtual sessao) {
		return sessao.perfil() == Perfil.OPERADOR;
	}
}
