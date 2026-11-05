package com.projeto.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.projeto.dto.AnaliseDTO;

@Service
public class CsvMatriz {

	public byte[] gerar(AnaliseDTO.Response analise) {
		StringBuilder texto = new StringBuilder();
		texto.append("Produto;Categoria;Quantidade;Popularidade (%);Margem (R$);Classe;Sugestão\n");
		for (AnaliseDTO.Item linha : analise.itens()) {
			texto.append(campo(linha.nome())).append(';')
					.append(campo(linha.categoria())).append(';')
					.append(linha.quantidade()).append(';')
					.append(decimal(linha.popularidade())).append(';')
					.append(decimal(linha.margem())).append(';')
					.append(campo(linha.classe().getRotulo())).append(';')
					.append(campo(linha.recomendacao())).append('\n');
		}
		byte[] corpo = texto.toString().getBytes(StandardCharsets.UTF_8);
		byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
		byte[] saida = Arrays.copyOf(bom, bom.length + corpo.length);
		System.arraycopy(corpo, 0, saida, bom.length, corpo.length);
		return saida;
	}

	private String campo(String valor) {
		if (valor == null) {
			return "";
		}
		String texto = valor;
		if (!texto.isEmpty() && "=+-@".indexOf(texto.charAt(0)) >= 0) {
			texto = "'" + texto;
		}
		if (texto.indexOf(';') >= 0 || texto.indexOf('"') >= 0 || texto.indexOf('\n') >= 0 || texto.indexOf('\r') >= 0) {
			return "\"" + texto.replace("\"", "\"\"") + "\"";
		}
		return texto;
	}

	private String decimal(BigDecimal valor) {
		return valor == null ? "" : valor.toPlainString().replace('.', ',');
	}
}
