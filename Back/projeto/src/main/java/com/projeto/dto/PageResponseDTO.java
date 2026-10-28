package com.projeto.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record PageResponseDTO<T>(
		List<T> conteudo,
		int pagina,
		int tamanho,
		long totalElementos,
		int totalPaginas,
		boolean ultima) {

	public static <T> PageResponseDTO<T> from(Page<T> page) {
		return new PageResponseDTO<>(
				page.getContent(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isLast());
	}
}
