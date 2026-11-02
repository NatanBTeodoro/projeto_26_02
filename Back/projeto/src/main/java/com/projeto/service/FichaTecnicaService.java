package com.projeto.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.service.AuditoriaService;
import com.projeto.dto.FichaTecnicaItemDTO;
import com.projeto.entity.FichaTecnicaItem;
import com.projeto.entity.Insumo;
import com.projeto.entity.PrecoCustoHistorico;
import com.projeto.entity.Produto;
import com.projeto.repository.FichaTecnicaItemRepository;
import com.projeto.repository.InsumoRepository;
import com.projeto.repository.PrecoCustoHistoricoRepository;
import com.projeto.repository.ProdutoRepository;
import com.projeto.seguranca.Acesso;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.util.exception.RecursoJaExistenteException;
import com.projeto.util.exception.RecursoNaoEncontradoException;
import com.projeto.util.exception.RequisicaoInvalidaException;

@Service
@Transactional(readOnly = true)
public class FichaTecnicaService {

	private final FichaTecnicaItemRepository fichaRepository;
	private final ProdutoRepository produtoRepository;
	private final InsumoRepository insumoRepository;
	private final PrecoCustoHistoricoRepository historicoRepository;
	private final AuditoriaService auditoriaService;

	public FichaTecnicaService(
			FichaTecnicaItemRepository fichaRepository,
			ProdutoRepository produtoRepository,
			InsumoRepository insumoRepository,
			PrecoCustoHistoricoRepository historicoRepository,
			AuditoriaService auditoriaService) {
		this.fichaRepository = fichaRepository;
		this.produtoRepository = produtoRepository;
		this.insumoRepository = insumoRepository;
		this.historicoRepository = historicoRepository;
		this.auditoriaService = auditoriaService;
	}

	public Page<FichaTecnicaItemDTO.Response> listar(Long produtoId, Pageable pageable, UsuarioAtual sessao) {
		carregarProduto(produtoId, sessao);
		return fichaRepository.findByProdutoId(produtoId, pageable).map(FichaTecnicaItemDTO.Response::from);
	}

	@Transactional
	public FichaTecnicaItemDTO.Response criar(Long produtoId, FichaTecnicaItemDTO.Create dto, UsuarioAtual sessao) {
		Acesso.exigirEscritaCadastro(sessao);
		if (!produtoId.equals(dto.produtoId())) {
			throw new RequisicaoInvalidaException("O produto da ficha não confere com a URL");
		}
		Produto produto = carregarProduto(produtoId, sessao);
		if (fichaRepository.existsByProdutoIdAndInsumoId(produtoId, dto.insumoId())) {
			throw new RecursoJaExistenteException("Insumo já está na ficha técnica");
		}
		Insumo insumo = insumoRepository.findByIdAndEstabelecimentoId(dto.insumoId(), sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Insumo", dto.insumoId()));
		if (insumo.getCustoUnitario() == null) {
			throw new RequisicaoInvalidaException("Insumo sem custo unitário");
		}
		FichaTecnicaItem item = new FichaTecnicaItem();
		item.setProduto(produto);
		item.setInsumo(insumo);
		item.setQuantidade(dto.quantidade());
		FichaTecnicaItem salvo = fichaRepository.save(item);
		recalcularCusto(produto);
		auditoriaService.registrar(sessao, "CRIAR", "ficha_tecnica", salvo.getId(), null, insumo.getNome());
		return FichaTecnicaItemDTO.Response.from(salvo);
	}

	private void recalcularCusto(Produto produto) {
		BigDecimal custo = BigDecimal.ZERO;
		for (FichaTecnicaItem item : fichaRepository.findByProdutoId(produto.getId())) {
			if (item.getInsumo().getCustoUnitario() == null) {
				throw new RequisicaoInvalidaException("Insumo sem custo unitário");
			}
			custo = custo.add(item.getQuantidade().multiply(item.getInsumo().getCustoUnitario()));
		}
		custo = custo.setScale(2, RoundingMode.HALF_UP);
		produto.setCustoAtual(custo);
		produtoRepository.save(produto);
		historicoRepository.findFirstByProdutoIdAndVigenciaFimIsNullOrderByVigenciaIniDesc(produto.getId())
				.ifPresent(aberto -> {
					aberto.setVigenciaFim(Instant.now());
					historicoRepository.save(aberto);
				});
		PrecoCustoHistorico novo = new PrecoCustoHistorico();
		novo.setProduto(produto);
		novo.setPreco(produto.getPrecoAtual());
		novo.setCusto(custo);
		novo.setVigenciaIni(Instant.now());
		historicoRepository.save(novo);
	}

	private Produto carregarProduto(Long id, UsuarioAtual sessao) {
		return produtoRepository.findByIdAndEstabelecimentoId(id, sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
	}
}
