package com.projeto.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.projeto.entity.enums.ClasseMenu;

/**
 * Cálculo puro de margem, popularidade, matriz e regras de associação.
 * Não depende de Spring, JPA nem HTTP.
 */
public final class MotorMenu {

	public static final int PEDIDOS_MINIMOS = 200;
	public static final int PARES_PARA_NAO_FRAGIL = 10;
	private static final BigDecimal CEM = new BigDecimal("100");
	private static final BigDecimal SETENTA = new BigDecimal("70");
	private static final BigDecimal FOLGA_FRAGIL = new BigDecimal("1.25");

	private MotorMenu() {
	}

	public static BigDecimal margem(BigDecimal preco, BigDecimal custo) {
		if (preco == null || custo == null) {
			return null;
		}
		return preco.subtract(custo).setScale(2, RoundingMode.HALF_UP);
	}

	public static BigDecimal popularidadePercentual(long quantidadeItem, long quantidadeTotal) {
		if (quantidadeTotal <= 0 || quantidadeItem <= 0) {
			return BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
		}
		return BigDecimal.valueOf(quantidadeItem)
				.multiply(CEM)
				.divide(BigDecimal.valueOf(quantidadeTotal), 6, RoundingMode.HALF_UP);
	}

	public static BigDecimal limitePopularidade(int itensElegiveis) {
		if (itensElegiveis <= 0) {
			return CEM.setScale(6, RoundingMode.HALF_UP);
		}
		return SETENTA.divide(BigDecimal.valueOf(itensElegiveis), 6, RoundingMode.HALF_UP);
	}

	public static boolean popular(BigDecimal participacaoPercentual, int itensElegiveis) {
		if (participacaoPercentual == null) {
			return false;
		}
		return participacaoPercentual.compareTo(limitePopularidade(itensElegiveis)) >= 0;
	}

	public static boolean margemAlta(BigDecimal margemItem, BigDecimal media) {
		if (margemItem == null || media == null) {
			return false;
		}
		return margemItem.compareTo(media) > 0;
	}

	public static ClasseMenu classe(boolean itemPopular, boolean margemAlta) {
		if (itemPopular && margemAlta) {
			return ClasseMenu.ESTRELA;
		}
		if (itemPopular) {
			return ClasseMenu.VACA_LEITEIRA;
		}
		if (margemAlta) {
			return ClasseMenu.ENIGMA;
		}
		return ClasseMenu.CAO;
	}

	public static BigDecimal mediaPonderada(List<BigDecimal> margens, List<Long> quantidades) {
		BigDecimal soma = BigDecimal.ZERO;
		long total = 0;
		for (int i = 0; i < margens.size(); i++) {
			BigDecimal margem = margens.get(i);
			long quantidade = quantidades.get(i);
			if (margem == null || quantidade <= 0) {
				continue;
			}
			soma = soma.add(margem.multiply(BigDecimal.valueOf(quantidade)));
			total += quantidade;
		}
		if (total == 0) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}
		return soma.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
	}

	public static BigDecimal mediaSimples(List<BigDecimal> margens) {
		BigDecimal soma = BigDecimal.ZERO;
		int n = 0;
		for (BigDecimal margem : margens) {
			if (margem == null) {
				continue;
			}
			soma = soma.add(margem);
			n++;
		}
		if (n == 0) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}
		return soma.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
	}

	public static ResultadoMineracao minerar(
			List<Set<String>> cestas,
			BigDecimal suporteMin,
			BigDecimal confiancaMin,
			BigDecimal liftMin,
			boolean exigirBaseMinima) {
		int total = cestas == null ? 0 : cestas.size();
		if (exigirBaseMinima && total < PEDIDOS_MINIMOS) {
			return new ResultadoMineracao(true, List.of());
		}
		if (total == 0) {
			return new ResultadoMineracao(false, List.of());
		}
		Map<String, Integer> porItem = new HashMap<>();
		Map<String, Integer> porPar = new HashMap<>();
		for (Set<String> cesta : cestas) {
			List<String> itens = new ArrayList<>(cesta);
			Collections.sort(itens);
			for (String item : itens) {
				porItem.merge(item, 1, Integer::sum);
			}
			if (itens.size() < 2) {
				continue;
			}
			for (int i = 0; i < itens.size(); i++) {
				for (int j = i + 1; j < itens.size(); j++) {
					porPar.merge(itens.get(i) + "\0" + itens.get(j), 1, Integer::sum);
				}
			}
		}
		List<RegraMinerada> regras = new ArrayList<>();
		for (Map.Entry<String, Integer> par : porPar.entrySet()) {
			String[] lados = par.getKey().split("\0", 2);
			int ambos = par.getValue();
			adicionarSeValida(regras, lados[0], lados[1], ambos, porItem, total, suporteMin, confiancaMin, liftMin);
			adicionarSeValida(regras, lados[1], lados[0], ambos, porItem, total, suporteMin, confiancaMin, liftMin);
		}
		regras.sort((a, b) -> b.lift().compareTo(a.lift()));
		return new ResultadoMineracao(false, List.copyOf(regras));
	}

	public static int consecutivosCao(List<ClasseMenu> doMaisRecenteAoMaisAntigo) {
		int seguidos = 0;
		for (ClasseMenu classe : doMaisRecenteAoMaisAntigo) {
			if (classe != ClasseMenu.CAO) {
				break;
			}
			seguidos++;
		}
		return seguidos;
	}

	public static String recomendacao(ClasseMenu classe, boolean antecedenteForte) {
		if (classe == null) {
			return "Sem classificação";
		}
		return switch (classe) {
			case ESTRELA -> "Manter em destaque no cardápio.";
			case VACA_LEITEIRA -> "Avaliar aumento de preço: o item vende bem e a margem está abaixo da média.";
			case ENIGMA -> "Promover ou combinar com um item popular para aumentar a saída.";
			case CAO -> antecedenteForte
					? "Não remover sem avaliar o cross-sell: o item puxa outras vendas."
					: "Candidato a remoção ou reformulação.";
			case EM_OBSERVACAO -> "Aguardar o histórico mínimo antes de decidir.";
			case DADO_INCOMPLETO -> "Completar o custo de produção para entrar na matriz.";
		};
	}

	private static void adicionarSeValida(
			List<RegraMinerada> regras,
			String antecedente,
			String consequente,
			int ambos,
			Map<String, Integer> porItem,
			int total,
			BigDecimal suporteMin,
			BigDecimal confiancaMin,
			BigDecimal liftMin) {
		int suporteAntecedente = porItem.getOrDefault(antecedente, 0);
		int suporteConsequente = porItem.getOrDefault(consequente, 0);
		if (suporteAntecedente == 0 || suporteConsequente == 0) {
			return;
		}
		BigDecimal suporte = razao(ambos, total);
		BigDecimal confianca = razao(ambos, suporteAntecedente);
		BigDecimal suporteConsequenteFracao = razao(suporteConsequente, total);
		if (suporteConsequenteFracao.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		BigDecimal lift = confianca.divide(suporteConsequenteFracao, 6, RoundingMode.HALF_UP);
		if (suporte.compareTo(suporteMin) < 0 || confianca.compareTo(confiancaMin) < 0 || lift.compareTo(liftMin) <= 0) {
			return;
		}
		boolean fragil = ambos < PARES_PARA_NAO_FRAGIL
				|| suporte.compareTo(suporteMin.multiply(FOLGA_FRAGIL)) < 0;
		regras.add(new RegraMinerada(antecedente, consequente, suporte, confianca, lift, fragil));
	}

	private static BigDecimal razao(int parte, int todo) {
		return BigDecimal.valueOf(parte).divide(BigDecimal.valueOf(todo), 6, RoundingMode.HALF_UP);
	}

	public record RegraMinerada(
			String antecedente,
			String consequente,
			BigDecimal suporte,
			BigDecimal confianca,
			BigDecimal lift,
			boolean fragil) {
	}

	public record ResultadoMineracao(boolean baseInsuficiente, List<RegraMinerada> regras) {
	}
}
