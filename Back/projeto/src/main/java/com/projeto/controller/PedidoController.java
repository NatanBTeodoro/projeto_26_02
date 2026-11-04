package com.projeto.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.PedidoDTO;
import com.projeto.service.PedidoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

	private final PedidoService pedidoService;

	public PedidoController(PedidoService pedidoService) {
		this.pedidoService = pedidoService;
	}

	@GetMapping
	public List<PedidoDTO.Response> listar() {
		return pedidoService.listar();
	}

	@PostMapping
	public PedidoDTO.Response criar(@Valid @RequestBody PedidoDTO.Create dto) {
		return pedidoService.criar(dto);
	}

	@PostMapping("/{id}/estorno")
	public PedidoDTO.Response estornar(@PathVariable Long id, @Valid @RequestBody PedidoDTO.Estorno dto) {
		return pedidoService.estornar(id, dto);
	}
}
