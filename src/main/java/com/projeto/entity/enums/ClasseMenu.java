package com.projeto.entity.enums;

public enum ClasseMenu {
	ESTRELA(
			"Estrela",
			"Manter o padrão de qualidade e dar destaque no cardápio. Evite mexer no preço sem testar."),
	VACA_LEITEIRA(
			"Vaca leiteira",
			"Vende bem, mas a margem é baixa. Teste um pequeno aumento de preço ou reduza o custo (porção, fornecedor)."),
	ENIGMA(
			"Enigma",
			"Margem boa, pouca saída. Promova o item: posição de destaque, sugestão do garçom, combo."),
	CAO(
			"Cão",
			"Pouca saída e pouca margem. Reformule a receita ou o preço, ou retire do cardápio."),
	DADO_INCOMPLETO(
			"Dado incompleto",
			"Cadastre o custo do produto para que ele seja classificado.");

	private final String rotulo;
	private final String recomendacao;

	ClasseMenu(String rotulo, String recomendacao) {
		this.rotulo = rotulo;
		this.recomendacao = recomendacao;
	}

	public String getRotulo() {
		return rotulo;
	}

	public String getRecomendacao() {
		return recomendacao;
	}
}
