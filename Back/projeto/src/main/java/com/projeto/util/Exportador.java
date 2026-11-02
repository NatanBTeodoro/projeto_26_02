package com.projeto.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.projeto.dto.RelatorioDTO;
import com.projeto.entity.ResultadoClassificacao;
import com.projeto.entity.ResultadoRegra;

public final class Exportador {

	private Exportador() {
	}

	public static byte[] csvAnalise(List<ResultadoClassificacao> itens, List<ResultadoRegra> regras) {
		StringBuilder csv = new StringBuilder();
		csv.append("tipo;produto_id;nome;popularidade;margem;classe;antecedente;consequente;suporte;confianca;lift;fragil\n");
		for (ResultadoClassificacao item : itens) {
			csv.append("classificacao;")
					.append(item.getProduto().getId()).append(';')
					.append(celula(item.getProduto().getNome())).append(';')
					.append(item.getPopularidade()).append(';')
					.append(item.getMargem()).append(';')
					.append(item.getClasse()).append(";;;;;;\n");
		}
		for (ResultadoRegra regra : regras) {
			csv.append("regra;;;;;;")
					.append(celula(regra.getAntecedente())).append(';')
					.append(celula(regra.getConsequente())).append(';')
					.append(regra.getSuporte()).append(';')
					.append(regra.getConfianca()).append(';')
					.append(regra.getLift()).append(';')
					.append(regra.isFragil()).append('\n');
		}
		return csv.toString().getBytes(StandardCharsets.UTF_8);
	}

	public static byte[] pdfAnalise(List<ResultadoClassificacao> itens, List<ResultadoRegra> regras) {
		return pdf(documento -> {
			documento.add(new Paragraph("Matriz e cross-sell"));
			for (ResultadoClassificacao item : itens) {
				documento.add(new Paragraph(item.getProduto().getNome() + " — " + item.getClasse()
						+ " — pop. " + item.getPopularidade() + "% — margem " + item.getMargem()));
			}
			documento.add(new Paragraph("Regras"));
			for (ResultadoRegra regra : regras) {
				documento.add(new Paragraph(regra.getAntecedente() + " -> " + regra.getConsequente()
						+ " lift " + regra.getLift() + (regra.isFragil() ? " (frágil)" : "")));
			}
		});
	}

	public static byte[] csvAbc(RelatorioDTO.Abc abc) {
		StringBuilder csv = new StringBuilder();
		csv.append("tipo;nome;categoria;receita;margem;participacao;classe\n");
		for (RelatorioDTO.AbcItem item : abc.itens()) {
			csv.append("item;").append(celula(item.nome())).append(';').append(celula(item.categoria())).append(';')
					.append(item.receita()).append(';').append(item.margem()).append(';')
					.append(item.participacao()).append(';').append(item.classeAbc()).append('\n');
		}
		for (RelatorioDTO.AbcCategoria categoria : abc.categorias()) {
			csv.append("categoria;").append(celula(categoria.categoria())).append(';').append(';')
					.append(categoria.receita()).append(';').append(categoria.margem()).append(';')
					.append(categoria.participacaoReceita()).append(";\n");
		}
		return csv.toString().getBytes(StandardCharsets.UTF_8);
	}

	public static byte[] pdfAbc(RelatorioDTO.Abc abc) {
		return pdf(documento -> {
			documento.add(new Paragraph("Curva ABC e categorias"));
			for (RelatorioDTO.AbcItem item : abc.itens()) {
				documento.add(new Paragraph(item.classeAbc() + " " + item.nome() + " " + item.receita()));
			}
			for (RelatorioDTO.AbcCategoria categoria : abc.categorias()) {
				documento.add(new Paragraph(categoria.categoria() + " receita " + categoria.receita()
						+ " margem " + categoria.margem()));
			}
		});
	}

	private static byte[] pdf(Escritor escritor) {
		try {
			ByteArrayOutputStream saida = new ByteArrayOutputStream();
			Document documento = new Document();
			PdfWriter.getInstance(documento, saida);
			documento.open();
			escritor.escrever(documento);
			documento.close();
			return saida.toByteArray();
		} catch (DocumentException | IOException ex) {
			throw new IllegalStateException("Falha ao gerar PDF");
		}
	}

	private static String celula(String valor) {
		if (valor == null) {
			return "";
		}
		String limpo = valor.replace(";", ",");
		if (limpo.startsWith("=") || limpo.startsWith("+") || limpo.startsWith("-") || limpo.startsWith("@")) {
			return "'" + limpo;
		}
		return limpo;
	}

	@FunctionalInterface
	private interface Escritor {
		void escrever(Document documento) throws DocumentException, IOException;
	}
}
