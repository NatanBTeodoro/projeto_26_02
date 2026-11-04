package com.projeto.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.CategoriaDTO;
import com.projeto.service.CategoriaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

	private final CategoriaService categoriaService;

	public CategoriaController(CategoriaService categoriaService) {
		this.categoriaService = categoriaService;
	}

	@GetMapping
	public List<CategoriaDTO.Response> listar() {
		return categoriaService.listar();
	}

	@PostMapping
	public CategoriaDTO.Response criar(@Valid @RequestBody CategoriaDTO.Create dto) {
		return categoriaService.criar(dto);
	}

	@PutMapping("/{id}")
	public CategoriaDTO.Response atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaDTO.Update dto) {
		return categoriaService.atualizar(id, dto);
	}
}
