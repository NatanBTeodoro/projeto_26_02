package com.projeto.controller;

import com.projeto.service.PedidoService;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.PageResponseDTO;
import com.projeto.dto.PedidoDTO;
import com.projeto.seguranca.SessaoAtual;
import com.projeto.util.HttpRespostas;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

	private final PedidoService pedidoService;
	private final SessaoAtual sessaoAtual;

	public PedidoController(PedidoService pedidoService, SessaoAtual sessaoAtual) {
		this.pedidoService = pedidoService;
		this.sessaoAtual = sessaoAtual;
	}

	@GetMapping
	public PageResponseDTO<PedidoDTO.Response> listar(
			@PageableDefault(size = 20, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
		return PageResponseDTO.from(pedidoService.listar(pageable, sessaoAtual.exigir()));
	}

	@GetMapping("/{id}")
	public PedidoDTO.Response buscar(@PathVariable Long id) {
		return pedidoService.buscar(id, sessaoAtual.exigir());
	}

	@PostMapping
	public ResponseEntity<PedidoDTO.Response> criar(@Valid @RequestBody PedidoDTO.Create dto) {
		PedidoDTO.Response corpo = pedidoService.criar(dto, sessaoAtual.exigir());
		return HttpRespostas.created(URI.create("/api/pedidos/" + corpo.id()), corpo);
	}

	@PostMapping("/{id}/estorno")
	public PedidoDTO.Response estornar(@PathVariable Long id, @Valid @RequestBody PedidoDTO.Estorno dto) {
		return pedidoService.estornar(id, dto, sessaoAtual.exigir());
	}
}
