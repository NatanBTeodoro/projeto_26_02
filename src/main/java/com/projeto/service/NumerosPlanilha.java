package com.projeto.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class NumerosPlanilha {

	private NumerosPlanilha() {
	}

	public record Leitura(BigDecimal valor, boolean vazio, String erro) {
	}

	public static Leitura ler(String texto) {
		if (texto == null) {
			return new Leitura(null, true, null);
		}
		String limpo = texto
				.replace("\u00a0", "")
				.replace("R$", "")
				.replace("r$", "")
				.replace(" ", "")
				.trim();
		if (limpo.isEmpty()) {
			return new Leitura(null, true, null);
		}
		int virgula = limpo.lastIndexOf(',');
		int ponto = limpo.lastIndexOf('.');
		if (virgula >= 0 && ponto >= 0) {
			if (virgula > ponto) {
				limpo = limpo.replace(".", "").replace(',', '.');
			} else {
				limpo = limpo.replace(",", "");
			}
		} else if (virgula >= 0) {
			limpo = limpo.replace(',', '.');
		}
		try {
			return new Leitura(new BigDecimal(limpo).setScale(2, RoundingMode.HALF_UP), false, null);
		} catch (NumberFormatException ex) {
			return new Leitura(null, false, "Número inválido");
		}
	}
}
