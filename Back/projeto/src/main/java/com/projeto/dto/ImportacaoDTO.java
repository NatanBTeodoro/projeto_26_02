package com.projeto.dto;

import com.projeto.entity.Importacao;
import com.projeto.entity.enums.StatusImportacao;

public final class ImportacaoDTO {

	private ImportacaoDTO() {
	}

	public record Response(
			Long id,
			Long estabelecimentoId,
			String arquivo,
			StatusImportacao status,
			Integer linhasOk,
			Integer linhasErro,
			String relatorioErros) {

		public static Response from(Importacao i) {
			return new Response(
					i.getId(),
					i.getEstabelecimento().getId(),
					i.getArquivo(),
					i.getStatus(),
					i.getLinhasOk(),
					i.getLinhasErro(),
					i.getRelatorioErros());
		}
	}
}
