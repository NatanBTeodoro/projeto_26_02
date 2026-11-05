package com.projeto.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.AnaliseDTO;
import com.projeto.service.AnaliseService;
import com.projeto.service.CsvMatriz;

@RestController
@RequestMapping("/api/analise")
public class AnaliseController {

	private final AnaliseService analiseService;
	private final CsvMatriz csvMatriz;

	public AnaliseController(AnaliseService analiseService, CsvMatriz csvMatriz) {
		this.analiseService = analiseService;
		this.csvMatriz = csvMatriz;
	}

	@GetMapping
	public AnaliseDTO.Response consultar(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
		return analiseService.consultar(de, ate);
	}

	@GetMapping("/exportar")
	public ResponseEntity<byte[]> exportar(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
		AnaliseDTO.Response analise = analiseService.consultar(de, ate);
		String nome = "matriz-" + de + "_a_" + ate + ".csv";
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome + "\"")
				.contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
				.body(csvMatriz.gerar(analise));
	}
}
