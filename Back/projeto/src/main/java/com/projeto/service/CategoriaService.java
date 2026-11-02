package com.projeto.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.dto.CategoriaDTO;
import com.projeto.entity.Categoria;
import com.projeto.entity.Estabelecimento;
import com.projeto.repository.CategoriaRepository;
import com.projeto.repository.EstabelecimentoRepository;
import com.projeto.seguranca.Acesso;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.util.exception.RecursoNaoEncontradoException;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

	private final CategoriaRepository categoriaRepository;
	private final EstabelecimentoRepository estabelecimentoRepository;

	public CategoriaService(
			CategoriaRepository categoriaRepository,
			EstabelecimentoRepository estabelecimentoRepository) {
		this.categoriaRepository = categoriaRepository;
		this.estabelecimentoRepository = estabelecimentoRepository;
	}

	public Page<CategoriaDTO.Response> listar(Pageable pageable, UsuarioAtual sessao) {
		return categoriaRepository.findByEstabelecimentoId(sessao.estabelecimentoId(), pageable)
				.map(CategoriaDTO.Response::from);
	}

	public CategoriaDTO.Response buscar(Long id, UsuarioAtual sessao) {
		return CategoriaDTO.Response.from(carregar(id, sessao));
	}

	@Transactional
	public CategoriaDTO.Response criar(CategoriaDTO.Create dto, UsuarioAtual sessao) {
		Acesso.exigirEscritaCadastro(sessao);
		Acesso.exigirMesmoEstabelecimento(dto.estabelecimentoId(), sessao);
		Estabelecimento estabelecimento = estabelecimentoRepository.findById(sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento", sessao.estabelecimentoId()));
		Categoria categoria = new Categoria();
		categoria.setEstabelecimento(estabelecimento);
		categoria.setNome(dto.nome().trim());
		categoria.setAtiva(true);
		return CategoriaDTO.Response.from(categoriaRepository.save(categoria));
	}

	@Transactional
	public CategoriaDTO.Response atualizar(Long id, CategoriaDTO.Update dto, UsuarioAtual sessao) {
		Acesso.exigirEscritaCadastro(sessao);
		Categoria categoria = carregar(id, sessao);
		categoria.setNome(dto.nome().trim());
		if (dto.ativa() != null) {
			categoria.setAtiva(dto.ativa());
		}
		return CategoriaDTO.Response.from(categoriaRepository.save(categoria));
	}

	private Categoria carregar(Long id, UsuarioAtual sessao) {
		return categoriaRepository.findByIdAndEstabelecimentoId(id, sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
	}
}
