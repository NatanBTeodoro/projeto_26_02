package com.projeto.controller;

import com.projeto.service.CategoriaService;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.CategoriaDTO;
import com.projeto.dto.PageResponseDTO;
import com.projeto.seguranca.SessaoAtual;
import com.projeto.util.HttpRespostas;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

	private final CategoriaService categoriaService;
	private final SessaoAtual sessaoAtual;

	public CategoriaController(CategoriaService categoriaService, SessaoAtual sessaoAtual) {
		this.categoriaService = categoriaService;
		this.sessaoAtual = sessaoAtual;
	}

	@GetMapping
	public PageResponseDTO<CategoriaDTO.Response> listar(
			@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
		return PageResponseDTO.from(categoriaService.listar(pageable, sessaoAtual.exigir()));
	}

	@GetMapping("/{id}")
	public CategoriaDTO.Response buscar(@PathVariable Long id) {
		return categoriaService.buscar(id, sessaoAtual.exigir());
	}

	@PostMapping
	public ResponseEntity<CategoriaDTO.Response> criar(@Valid @RequestBody CategoriaDTO.Create dto) {
		CategoriaDTO.Response corpo = categoriaService.criar(dto, sessaoAtual.exigir());
		return HttpRespostas.created(URI.create("/api/categorias/" + corpo.id()), corpo);
	}

	@PutMapping("/{id}")
	public CategoriaDTO.Response atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaDTO.Update dto) {
		return categoriaService.atualizar(id, dto, sessaoAtual.exigir());
	}
}
