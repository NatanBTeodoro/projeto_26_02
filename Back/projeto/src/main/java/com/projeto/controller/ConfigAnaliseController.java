package com.projeto.controller;

import com.projeto.service.ConfigAnaliseService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.ConfigAnaliseDTO;
import com.projeto.seguranca.SessaoAtual;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/config-analise")
public class ConfigAnaliseController {

	private final ConfigAnaliseService configAnaliseService;
	private final SessaoAtual sessaoAtual;

	public ConfigAnaliseController(ConfigAnaliseService configAnaliseService, SessaoAtual sessaoAtual) {
		this.configAnaliseService = configAnaliseService;
		this.sessaoAtual = sessaoAtual;
	}

	@GetMapping
	public ConfigAnaliseDTO.Response buscar() {
		return configAnaliseService.buscar(sessaoAtual.exigir());
	}

	@PutMapping
	public ConfigAnaliseDTO.Response atualizar(@Valid @RequestBody ConfigAnaliseDTO.Update dto) {
		return configAnaliseService.atualizar(dto, sessaoAtual.exigir());
	}
}
