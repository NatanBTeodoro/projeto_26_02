package com.projeto.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.service.CalculadoraCardapio;
import com.projeto.service.ConsultaVendas;
import com.projeto.service.ConfigAnaliseService;
import com.projeto.dto.AnaliseDTO;
import com.projeto.dto.RelatorioDTO;
import com.projeto.entity.ConfigAnalise;
import com.projeto.entity.Unidade;
import com.projeto.entity.enums.ClasseMenu;
import com.projeto.entity.enums.Granularidade;
import com.projeto.repository.AlertaRepository;
import com.projeto.repository.UnidadeRepository;
import com.projeto.seguranca.Acesso;
import com.projeto.seguranca.UsuarioAtual;

@Service
@Transactional(readOnly = true)
public class RelatorioService {

	private final ConsultaVendas consultaVendas;
	private final ConfigAnaliseService configAnaliseService;
	private final AlertaRepository alertaRepository;
	private final UnidadeRepository unidadeRepository;

	public RelatorioService(
			ConsultaVendas consultaVendas,
			ConfigAnaliseService configAnaliseService,
			AlertaRepository alertaRepository,
			UnidadeRepository unidadeRepository) {
		this.consultaVendas = consultaVendas;
		this.configAnaliseService = configAnaliseService;
		this.alertaRepository = alertaRepository;
		this.unidadeRepository = unidadeRepository;
	}

	public RelatorioDTO.Dashboard dashboard(Instant inicio, Instant fim, UsuarioAtual sessao) {
		List<ConsultaVendas.Linha> linhas = consultaVendas.linhas(sessao.estabelecimentoId(), inicio, fim);
		CalculadoraCardapio.Saida saida = calcular(sessao.estabelecimentoId(), linhas, inicio, fim, Granularidade.MES);
		BigDecimal receita = BigDecimal.ZERO;
		Map<Long, BigDecimal> porPedido = new LinkedHashMap<>();
		for (ConsultaVendas.Linha linha : linhas) {
			if (linha.precoUnitario() == null) {
				continue;
			}
			BigDecimal parcial = linha.precoUnitario().multiply(BigDecimal.valueOf(linha.quantidade()));
			receita = receita.add(parcial);
			porPedido.merge(linha.pedidoId(), parcial, BigDecimal::add);
		}
		BigDecimal ticket = porPedido.isEmpty()
				? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
				: receita.divide(BigDecimal.valueOf(porPedido.size()), 2, RoundingMode.HALF_UP);
		Map<String, Long> porClasse = new LinkedHashMap<>();
		for (ClasseMenu classe : ClasseMenu.values()) {
			porClasse.put(classe.name(), 0L);
		}
		for (var item : saida.itens()) {
			porClasse.merge(item.classe().name(), 1L, Long::sum);
		}
		List<AnaliseDTO.RegraResponse> regras = saida.mineracao().regras().stream()
				.limit(5)
				.map(regra -> new AnaliseDTO.RegraResponse(
						null,
						regra.antecedente(),
						regra.consequente(),
						regra.suporte(),
						regra.confianca(),
						regra.lift(),
						regra.fragil()))
				.toList();
		return new RelatorioDTO.Dashboard(
				receita.setScale(2, RoundingMode.HALF_UP),
				ticket,
				porPedido.size(),
				porClasse,
				alertaRepository.countByProdutoEstabelecimentoIdAndAtivoTrue(sessao.estabelecimentoId()),
				regras,
				saida.mineracao().baseInsuficiente());
	}

	public RelatorioDTO.Abc abc(Instant inicio, Instant fim, UsuarioAtual sessao) {
		Acesso.exigirLeituraAnalise(sessao);
		List<ConsultaVendas.Linha> linhas = consultaVendas.linhas(sessao.estabelecimentoId(), inicio, fim);
		Map<Long, Acumulo> produtos = new LinkedHashMap<>();
		for (ConsultaVendas.Linha linha : linhas) {
			if (linha.precoUnitario() == null || linha.precoUnitario().compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}
			produtos.computeIfAbsent(linha.produtoId(), id -> new Acumulo(linha.produtoNome(), linha.categoriaNome()))
					.somar(linha);
		}
		BigDecimal receitaTotal = produtos.values().stream().map(Acumulo::receita).reduce(BigDecimal.ZERO, BigDecimal::add);
		List<Acumulo> ordenados = new ArrayList<>(produtos.values());
		ordenados.sort(Comparator.comparing(Acumulo::receita).reversed());
		BigDecimal acumulado = BigDecimal.ZERO;
		List<RelatorioDTO.AbcItem> itens = new ArrayList<>();
		int indice = 0;
		for (Acumulo acumulo : ordenados) {
			indice++;
			acumulado = acumulado.add(acumulo.receita());
			BigDecimal participacao = proporcao(acumulo.receita(), receitaTotal);
			BigDecimal acumulada = proporcao(acumulado, receitaTotal);
			String classe = acumulada.compareTo(new BigDecimal("0.80")) <= 0 || indice == 1
					? "A"
					: acumulada.compareTo(new BigDecimal("0.95")) <= 0 ? "B" : "C";
			itens.add(new RelatorioDTO.AbcItem(
					null,
					acumulo.nome(),
					acumulo.categoria(),
					acumulo.receita().setScale(2, RoundingMode.HALF_UP),
					acumulo.margem().setScale(2, RoundingMode.HALF_UP),
					participacao,
					classe));
		}
		Map<String, Acumulo> categorias = new LinkedHashMap<>();
		for (Acumulo acumulo : ordenados) {
			categorias.computeIfAbsent(acumulo.categoria(), Acumulo::categoriaVazia).somarTotais(acumulo);
		}
		BigDecimal margemTotal = ordenados.stream().map(Acumulo::margem).reduce(BigDecimal.ZERO, BigDecimal::add);
		List<RelatorioDTO.AbcCategoria> porCategoria = new ArrayList<>();
		for (Acumulo categoria : categorias.values()) {
			porCategoria.add(new RelatorioDTO.AbcCategoria(
					categoria.nome(),
					categoria.receita().setScale(2, RoundingMode.HALF_UP),
					categoria.margem().setScale(2, RoundingMode.HALF_UP),
					proporcao(categoria.receita(), receitaTotal),
					proporcao(categoria.margem(), margemTotal)));
		}
		return new RelatorioDTO.Abc(itens, porCategoria);
	}

	public List<RelatorioDTO.Cliente> clientes(Instant inicio, Instant fim, UsuarioAtual sessao) {
		Acesso.exigirEscritaAnalise(sessao);
		Map<String, List<ConsultaVendas.Linha>> grupos = new LinkedHashMap<>();
		for (ConsultaVendas.Linha linha : consultaVendas.linhas(sessao.estabelecimentoId(), inicio, fim)) {
			if (linha.clienteHash() == null) {
				continue;
			}
			grupos.computeIfAbsent(linha.clienteHash(), hash -> new ArrayList<>()).add(linha);
		}
		List<RelatorioDTO.Cliente> resposta = new ArrayList<>();
		for (var entrada : grupos.entrySet()) {
			Map<Long, Instant> pedidos = new LinkedHashMap<>();
			BigDecimal receita = BigDecimal.ZERO;
			for (ConsultaVendas.Linha linha : entrada.getValue()) {
				pedidos.putIfAbsent(linha.pedidoId(), linha.dataHora());
				if (linha.precoUnitario() != null) {
					receita = receita.add(linha.precoUnitario().multiply(BigDecimal.valueOf(linha.quantidade())));
				}
			}
			List<Instant> datas = pedidos.values().stream().sorted().toList();
			BigDecimal intervalo = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
			if (datas.size() > 1) {
				long segundos = 0;
				for (int i = 1; i < datas.size(); i++) {
					segundos += Duration.between(datas.get(i - 1), datas.get(i)).getSeconds();
				}
				intervalo = BigDecimal.valueOf(segundos)
						.divide(BigDecimal.valueOf(3600L * (datas.size() - 1)), 2, RoundingMode.HALF_UP);
			}
			BigDecimal ticket = pedidos.isEmpty()
					? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
					: receita.divide(BigDecimal.valueOf(pedidos.size()), 2, RoundingMode.HALF_UP);
			String visivel = entrada.getKey().substring(0, Math.min(12, entrada.getKey().length()));
			resposta.add(new RelatorioDTO.Cliente(visivel, pedidos.size(), ticket, intervalo));
		}
		return resposta;
	}

	public List<RelatorioDTO.UnidadeDesempenho> unidades(Instant inicio, Instant fim, UsuarioAtual sessao) {
		Acesso.exigirEscritaAnalise(sessao);
		Map<Long, Unidade> cadastro = new HashMap<>();
		for (Unidade unidade : unidadeRepository.findByEstabelecimentoIdAndAtivaTrue(sessao.estabelecimentoId())) {
			cadastro.put(unidade.getId(), unidade);
		}
		Map<Long, BigDecimal> receita = new HashMap<>();
		Map<Long, Map<Long, Boolean>> pedidos = new HashMap<>();
		for (ConsultaVendas.Linha linha : consultaVendas.linhas(sessao.estabelecimentoId(), inicio, fim)) {
			if (linha.unidadeId() == null) {
				continue;
			}
			BigDecimal parcial = linha.precoUnitario() == null
					? BigDecimal.ZERO
					: linha.precoUnitario().multiply(BigDecimal.valueOf(linha.quantidade()));
			receita.merge(linha.unidadeId(), parcial, BigDecimal::add);
			pedidos.computeIfAbsent(linha.unidadeId(), id -> new LinkedHashMap<>()).put(linha.pedidoId(), true);
		}
		List<RelatorioDTO.UnidadeDesempenho> resposta = new ArrayList<>();
		for (Unidade unidade : cadastro.values()) {
			BigDecimal total = receita.getOrDefault(unidade.getId(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
			long quantidade = pedidos.getOrDefault(unidade.getId(), Map.of()).size();
			BigDecimal ticket = quantidade == 0
					? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
					: total.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.HALF_UP);
			resposta.add(new RelatorioDTO.UnidadeDesempenho(unidade.getId(), unidade.getNome(), total, quantidade, ticket));
		}
		return resposta;
	}

	private CalculadoraCardapio.Saida calcular(
			Long estabelecimentoId,
			List<ConsultaVendas.Linha> linhas,
			Instant inicio,
			Instant fim,
			Granularidade granularidade) {
		ConfigAnalise config = configAnaliseService.obterOuCriar(estabelecimentoId);
		return CalculadoraCardapio.calcular(
				linhas,
				new CalculadoraCardapio.Parametros(
						config.getSuporteMin(),
						config.getConfiancaMin(),
						config.getLiftMin(),
						config.getPeriodoMinimoDias(),
						config.isEscopoPorCategoria(),
						config.getCriterioPopularidade()),
				consultaVendas.primeirasVendas(estabelecimentoId),
				inicio,
				fim,
				granularidade);
	}

	private BigDecimal proporcao(BigDecimal parte, BigDecimal total) {
		if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
		}
		return parte.divide(total, 4, RoundingMode.HALF_UP);
	}

	private static final class Acumulo {
		private final String nome;
		private final String categoria;
		private BigDecimal receita = BigDecimal.ZERO;
		private BigDecimal margem = BigDecimal.ZERO;

		private Acumulo(String nome, String categoria) {
			this.nome = nome;
			this.categoria = categoria;
		}

		private static Acumulo categoriaVazia(String nome) {
			return new Acumulo(nome, nome);
		}

		private void somar(ConsultaVendas.Linha linha) {
			BigDecimal qtd = BigDecimal.valueOf(linha.quantidade());
			receita = receita.add(linha.precoUnitario().multiply(qtd));
			if (linha.custoUnitario() != null) {
				margem = margem.add(linha.precoUnitario().subtract(linha.custoUnitario()).multiply(qtd));
			}
		}

		private void somarTotais(Acumulo outro) {
			receita = receita.add(outro.receita);
			margem = margem.add(outro.margem);
		}

		private String nome() {
			return nome;
		}

		private String categoria() {
			return categoria;
		}

		private BigDecimal receita() {
			return receita;
		}

		private BigDecimal margem() {
			return margem;
		}
	}
}
