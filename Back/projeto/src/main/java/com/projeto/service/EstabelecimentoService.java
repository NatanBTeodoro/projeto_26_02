package com.projeto.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.dto.EstabelecimentoDTO;
import com.projeto.entity.Estabelecimento;
import com.projeto.repository.EstabelecimentoRepository;
import com.projeto.seguranca.Acesso;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.util.exception.RecursoNaoEncontradoException;

@Service
@Transactional(readOnly = true)
public class EstabelecimentoService {

	private final EstabelecimentoRepository estabelecimentoRepository;

	public EstabelecimentoService(EstabelecimentoRepository estabelecimentoRepository) {
		this.estabelecimentoRepository = estabelecimentoRepository;
	}

	public Page<EstabelecimentoDTO.Response> listar(Pageable pageable, UsuarioAtual sessao) {
		Estabelecimento estabelecimento = buscarDoUsuario(sessao);
		if (pageable.getPageNumber() > 0) {
			return new PageImpl<>(List.of(), pageable, 1);
		}
		return new PageImpl<>(List.of(EstabelecimentoDTO.Response.from(estabelecimento)), pageable, 1);
	}

	public EstabelecimentoDTO.Response buscar(Long id, UsuarioAtual sessao) {
		if (!id.equals(sessao.estabelecimentoId())) {
			throw new RecursoNaoEncontradoException("Estabelecimento", id);
		}
		return EstabelecimentoDTO.Response.from(buscarDoUsuario(sessao));
	}

	@Transactional
	public EstabelecimentoDTO.Response criar(EstabelecimentoDTO.Create dto, UsuarioAtual sessao) {
		Acesso.exigirAdministrador(sessao);
		Estabelecimento estabelecimento = new Estabelecimento();
		estabelecimento.setNome(dto.nome().trim());
		estabelecimento.setAtivo(true);
		return EstabelecimentoDTO.Response.from(estabelecimentoRepository.save(estabelecimento));
	}

	@Transactional
	public EstabelecimentoDTO.Response atualizar(Long id, EstabelecimentoDTO.Update dto, UsuarioAtual sessao) {
		Acesso.exigirEscritaCadastro(sessao);
		if (!id.equals(sessao.estabelecimentoId())) {
			throw new RecursoNaoEncontradoException("Estabelecimento", id);
		}
		Estabelecimento estabelecimento = buscarDoUsuario(sessao);
		estabelecimento.setNome(dto.nome().trim());
		if (dto.ativo() != null) {
			estabelecimento.setAtivo(dto.ativo());
		}
		return EstabelecimentoDTO.Response.from(estabelecimentoRepository.save(estabelecimento));
	}

	private Estabelecimento buscarDoUsuario(UsuarioAtual sessao) {
		return estabelecimentoRepository.findById(sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento", sessao.estabelecimentoId()));
	}
}
