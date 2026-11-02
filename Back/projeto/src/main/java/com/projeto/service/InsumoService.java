package com.projeto.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.service.AuditoriaService;
import com.projeto.dto.InsumoDTO;
import com.projeto.entity.Estabelecimento;
import com.projeto.entity.Insumo;
import com.projeto.repository.EstabelecimentoRepository;
import com.projeto.repository.InsumoRepository;
import com.projeto.seguranca.Acesso;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.util.exception.RecursoNaoEncontradoException;
import com.projeto.util.exception.RequisicaoInvalidaException;

@Service
@Transactional(readOnly = true)
public class InsumoService {

	private final InsumoRepository insumoRepository;
	private final EstabelecimentoRepository estabelecimentoRepository;
	private final AuditoriaService auditoriaService;

	public InsumoService(
			InsumoRepository insumoRepository,
			EstabelecimentoRepository estabelecimentoRepository,
			AuditoriaService auditoriaService) {
		this.insumoRepository = insumoRepository;
		this.estabelecimentoRepository = estabelecimentoRepository;
		this.auditoriaService = auditoriaService;
	}

	public Page<InsumoDTO.Response> listar(Pageable pageable, UsuarioAtual sessao) {
		return insumoRepository.findByEstabelecimentoId(sessao.estabelecimentoId(), pageable)
				.map(InsumoDTO.Response::from);
	}

	public InsumoDTO.Response buscar(Long id, UsuarioAtual sessao) {
		return InsumoDTO.Response.from(carregar(id, sessao));
	}

	@Transactional
	public InsumoDTO.Response criar(InsumoDTO.Create dto, UsuarioAtual sessao) {
		Acesso.exigirEscritaCadastro(sessao);
		Acesso.exigirMesmoEstabelecimento(dto.estabelecimentoId(), sessao);
		validarCusto(dto.custoUnitario());
		Estabelecimento estabelecimento = estabelecimentoRepository.findById(sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento", sessao.estabelecimentoId()));
		Insumo insumo = new Insumo();
		insumo.setEstabelecimento(estabelecimento);
		insumo.setNome(dto.nome().trim());
		insumo.setUnidadeMedida(dto.unidadeMedida().trim());
		insumo.setCustoUnitario(dto.custoUnitario());
		insumo.setAtivo(true);
		Insumo salvo = insumoRepository.save(insumo);
		auditoriaService.registrar(sessao, "CRIAR", "insumo", salvo.getId(), null, salvo.getNome());
		return InsumoDTO.Response.from(salvo);
	}

	@Transactional
	public InsumoDTO.Response atualizar(Long id, InsumoDTO.Update dto, UsuarioAtual sessao) {
		Acesso.exigirEscritaCadastro(sessao);
		validarCusto(dto.custoUnitario());
		Insumo insumo = carregar(id, sessao);
		insumo.setNome(dto.nome().trim());
		insumo.setUnidadeMedida(dto.unidadeMedida().trim());
		insumo.setCustoUnitario(dto.custoUnitario());
		if (dto.ativo() != null) {
			insumo.setAtivo(dto.ativo());
		}
		auditoriaService.registrar(sessao, "ATUALIZAR", "insumo", insumo.getId(), null, insumo.getNome());
		return InsumoDTO.Response.from(insumoRepository.save(insumo));
	}

	private void validarCusto(BigDecimal custo) {
		if (custo != null && custo.compareTo(BigDecimal.ZERO) < 0) {
			throw new RequisicaoInvalidaException("Custo unitário inválido");
		}
	}

	private Insumo carregar(Long id, UsuarioAtual sessao) {
		return insumoRepository.findByIdAndEstabelecimentoId(id, sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Insumo", id));
	}
}
