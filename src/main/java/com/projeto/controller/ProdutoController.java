package com.projeto.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.ProdutoDTO;
import com.projeto.service.ProdutoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

	private final ProdutoService produtoService;

	public ProdutoController(ProdutoService produtoService) {
		this.produtoService = produtoService;
	}

	@GetMapping
	public List<ProdutoDTO.Response> listar() {
		return produtoService.listar();
	}

	@PostMapping
	public ProdutoDTO.Response criar(@Valid @RequestBody ProdutoDTO.Create dto) {
		return produtoService.criar(dto);
	}

	@PutMapping("/{id}")
	public ProdutoDTO.Response atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoDTO.Update dto) {
		return produtoService.atualizar(id, dto);
	}
}
