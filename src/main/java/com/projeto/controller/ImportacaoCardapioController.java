package com.projeto.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.projeto.dto.ImportacaoCardapioDTO;
import com.projeto.service.ImportacaoCardapioService;
import com.projeto.util.exception.RegraDeNegocioException;

@RestController
@RequestMapping("/api/produtos/importar")
public class ImportacaoCardapioController {

	private final ImportacaoCardapioService importacaoCardapioService;

	public ImportacaoCardapioController(ImportacaoCardapioService importacaoCardapioService) {
		this.importacaoCardapioService = importacaoCardapioService;
	}

	@GetMapping("/modelo")
	public ResponseEntity<byte[]> modelo() {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"modelo-cardapio.csv\"")
				.contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
				.body(importacaoCardapioService.modelo());
	}

	@PostMapping(value = "/prever", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ImportacaoCardapioDTO.Previa prever(@RequestParam("arquivo") MultipartFile arquivo) {
		return importacaoCardapioService.prever(arquivo);
	}

	@PostMapping(value = "/confirmar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ImportacaoCardapioDTO.Resultado confirmar(
			@RequestParam("arquivo") MultipartFile arquivo,
			@RequestParam(name = "modo", defaultValue = "CANCELAR_TUDO") String modo) {
		ImportacaoCardapioDTO.Modo escolhido;
		try {
			escolhido = ImportacaoCardapioDTO.Modo.valueOf(modo);
		} catch (IllegalArgumentException ex) {
			throw new RegraDeNegocioException("Modo de importação inválido");
		}
		return importacaoCardapioService.confirmar(arquivo, escolhido);
	}
}
