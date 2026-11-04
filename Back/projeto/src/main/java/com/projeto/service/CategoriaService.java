package com.projeto.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.dto.CategoriaDTO;
import com.projeto.entity.Categoria;
import com.projeto.repository.CategoriaRepository;
import com.projeto.util.exception.RecursoNaoEncontradoException;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

	private final CategoriaRepository categoriaRepository;

	public CategoriaService(CategoriaRepository categoriaRepository) {
		this.categoriaRepository = categoriaRepository;
	}

	public List<CategoriaDTO.Response> listar() {
		return categoriaRepository.findAllByOrderByNomeAsc().stream().map(CategoriaDTO.Response::de).toList();
	}

	@Transactional
	public CategoriaDTO.Response criar(CategoriaDTO.Create dto) {
		Categoria categoria = new Categoria();
		categoria.setNome(dto.nome().trim());
		categoria.setAtiva(true);
		return CategoriaDTO.Response.de(categoriaRepository.save(categoria));
	}

	@Transactional
	public CategoriaDTO.Response atualizar(Long id, CategoriaDTO.Update dto) {
		Categoria categoria = carregar(id);
		categoria.setNome(dto.nome().trim());
		if (dto.ativa() != null) {
			categoria.setAtiva(dto.ativa());
		}
		return CategoriaDTO.Response.de(categoriaRepository.save(categoria));
	}

	Categoria carregar(Long id) {
		return categoriaRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
	}
}
