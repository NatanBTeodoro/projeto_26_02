package com.projeto.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.dto.PrecoCustoHistoricoDTO;
import com.projeto.dto.ProdutoDTO;
import com.projeto.entity.Categoria;
import com.projeto.entity.Estabelecimento;
import com.projeto.entity.PrecoCustoHistorico;
import com.projeto.entity.Produto;
import com.projeto.repository.CategoriaRepository;
import com.projeto.repository.EstabelecimentoRepository;
import com.projeto.repository.PrecoCustoHistoricoRepository;
import com.projeto.repository.ProdutoRepository;
import com.projeto.seguranca.Acesso;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.util.exception.RecursoNaoEncontradoException;
import com.projeto.util.exception.RequisicaoInvalidaException;

@Service
@Transactional(readOnly = true)
public class ProdutoService {

	private final ProdutoRepository produtoRepository;
	private final CategoriaRepository categoriaRepository;
	private final EstabelecimentoRepository estabelecimentoRepository;
	private final PrecoCustoHistoricoRepository historicoRepository;

	public ProdutoService(
			ProdutoRepository produtoRepository,
			CategoriaRepository categoriaRepository,
			EstabelecimentoRepository estabelecimentoRepository,
			PrecoCustoHistoricoRepository historicoRepository) {
		this.produtoRepository = produtoRepository;
		this.categoriaRepository = categoriaRepository;
		this.estabelecimentoRepository = estabelecimentoRepository;
		this.historicoRepository = historicoRepository;
	}

	public Page<ProdutoDTO.Response> listar(Pageable pageable, UsuarioAtual sessao) {
		return produtoRepository.findByEstabelecimentoId(sessao.estabelecimentoId(), pageable)
				.map(ProdutoDTO.Response::from);
	}

	public ProdutoDTO.Response buscar(Long id, UsuarioAtual sessao) {
		return ProdutoDTO.Response.from(carregar(id, sessao));
	}

	public Page<PrecoCustoHistoricoDTO.Response> listarHistorico(Long produtoId, Pageable pageable, UsuarioAtual sessao) {
		carregar(produtoId, sessao);
		return historicoRepository.findByProdutoId(produtoId, pageable).map(PrecoCustoHistoricoDTO.Response::from);
	}

	@Transactional
	public ProdutoDTO.Response criar(ProdutoDTO.Create dto, UsuarioAtual sessao) {
		Acesso.exigirEscritaCadastro(sessao);
		Acesso.exigirMesmoEstabelecimento(dto.estabelecimentoId(), sessao);
		Estabelecimento estabelecimento = estabelecimentoRepository.findById(sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento", sessao.estabelecimentoId()));
		Produto produto = new Produto();
		produto.setEstabelecimento(estabelecimento);
		produto.setAtivo(true);
		aplicar(produto, dto.nome(), dto.categoriaId(), dto.precoAtual(), dto.custoAtual(), dto.sazonal(),
				dto.janelaIni(), dto.janelaFim(), sessao);
		produto = produtoRepository.save(produto);
		registrarVigencia(produto, Instant.now());
		return ProdutoDTO.Response.from(produto);
	}

	@Transactional
	public ProdutoDTO.Response atualizar(Long id, ProdutoDTO.Update dto, UsuarioAtual sessao) {
		Acesso.exigirEscritaCadastro(sessao);
		Produto produto = carregar(id, sessao);
		boolean mudouValor = produto.getPrecoAtual().compareTo(dto.precoAtual()) != 0
				|| !iguais(produto.getCustoAtual(), dto.custoAtual());
		aplicar(produto, dto.nome(), dto.categoriaId(), dto.precoAtual(), dto.custoAtual(), dto.sazonal(),
				dto.janelaIni(), dto.janelaFim(), sessao);
		if (dto.ativo() != null) {
			produto.setAtivo(dto.ativo());
		}
		produto = produtoRepository.save(produto);
		if (mudouValor) {
			registrarVigencia(produto, Instant.now());
		}
		return ProdutoDTO.Response.from(produto);
	}

	private void aplicar(
			Produto produto,
			String nome,
			Long categoriaId,
			BigDecimal preco,
			BigDecimal custo,
			Boolean sazonal,
			LocalDate janelaIni,
			LocalDate janelaFim,
			UsuarioAtual sessao) {
		if (preco == null || preco.compareTo(BigDecimal.ZERO) < 0) {
			throw new RequisicaoInvalidaException("Preço de venda inválido");
		}
		if (custo != null && custo.compareTo(BigDecimal.ZERO) < 0) {
			throw new RequisicaoInvalidaException("Custo de produção inválido");
		}
		boolean ehSazonal = Boolean.TRUE.equals(sazonal);
		if (ehSazonal && (janelaIni == null || janelaFim == null || janelaFim.isBefore(janelaIni))) {
			throw new RequisicaoInvalidaException("Janela sazonal inválida");
		}
		produto.setNome(nome.trim());
		produto.setPrecoAtual(preco);
		produto.setCustoAtual(custo);
		produto.setSazonal(ehSazonal);
		produto.setJanelaIni(ehSazonal ? janelaIni : null);
		produto.setJanelaFim(ehSazonal ? janelaFim : null);
		if (categoriaId == null) {
			produto.setCategoria(null);
			return;
		}
		Categoria categoria = categoriaRepository
				.findByIdAndEstabelecimentoId(categoriaId, sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", categoriaId));
		produto.setCategoria(categoria);
	}

	private void registrarVigencia(Produto produto, Instant agora) {
		historicoRepository.findFirstByProdutoIdAndVigenciaFimIsNullOrderByVigenciaIniDesc(produto.getId())
				.ifPresent(aberto -> {
					aberto.setVigenciaFim(agora);
					historicoRepository.save(aberto);
				});
		PrecoCustoHistorico novo = new PrecoCustoHistorico();
		novo.setProduto(produto);
		novo.setPreco(produto.getPrecoAtual());
		novo.setCusto(produto.getCustoAtual());
		novo.setVigenciaIni(agora);
		historicoRepository.save(novo);
	}

	private Produto carregar(Long id, UsuarioAtual sessao) {
		return produtoRepository.findByIdAndEstabelecimentoId(id, sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
	}

	private boolean iguais(BigDecimal esquerda, BigDecimal direita) {
		if (esquerda == null || direita == null) {
			return esquerda == direita;
		}
		return esquerda.compareTo(direita) == 0;
	}
}
