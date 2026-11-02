package com.projeto.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.service.MotorMenu;
import com.projeto.util.Exportador;
import com.projeto.service.AuditoriaService;
import com.projeto.dto.AnaliseDTO;
import com.projeto.entity.Alerta;
import com.projeto.entity.AnaliseExecucao;
import com.projeto.entity.ConfigAnalise;
import com.projeto.entity.Estabelecimento;
import com.projeto.entity.Produto;
import com.projeto.entity.ResultadoClassificacao;
import com.projeto.entity.ResultadoRegra;
import com.projeto.entity.enums.ClasseMenu;
import com.projeto.entity.enums.CriterioPopularidade;
import com.projeto.entity.enums.StatusAnalise;
import com.projeto.entity.enums.TipoAlerta;
import com.projeto.repository.AlertaRepository;
import com.projeto.repository.AnaliseExecucaoRepository;
import com.projeto.repository.EstabelecimentoRepository;
import com.projeto.repository.ProdutoRepository;
import com.projeto.repository.ResultadoClassificacaoRepository;
import com.projeto.repository.ResultadoRegraRepository;
import com.projeto.seguranca.Acesso;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.util.exception.RecursoNaoEncontradoException;
import com.projeto.util.exception.RequisicaoInvalidaException;

@Service
@Transactional(readOnly = true)
public class AnaliseService {

	private final AnaliseExecucaoRepository execucaoRepository;
	private final ResultadoClassificacaoRepository classificacaoRepository;
	private final ResultadoRegraRepository regraRepository;
	private final ConfigAnaliseService configAnaliseService;
	private final EstabelecimentoRepository estabelecimentoRepository;
	private final ProdutoRepository produtoRepository;
	private final AlertaRepository alertaRepository;
	private final ConsultaVendas consultaVendas;
	private final AuditoriaService auditoriaService;

	public AnaliseService(
			AnaliseExecucaoRepository execucaoRepository,
			ResultadoClassificacaoRepository classificacaoRepository,
			ResultadoRegraRepository regraRepository,
			ConfigAnaliseService configAnaliseService,
			EstabelecimentoRepository estabelecimentoRepository,
			ProdutoRepository produtoRepository,
			AlertaRepository alertaRepository,
			ConsultaVendas consultaVendas,
			AuditoriaService auditoriaService) {
		this.execucaoRepository = execucaoRepository;
		this.classificacaoRepository = classificacaoRepository;
		this.regraRepository = regraRepository;
		this.configAnaliseService = configAnaliseService;
		this.estabelecimentoRepository = estabelecimentoRepository;
		this.produtoRepository = produtoRepository;
		this.alertaRepository = alertaRepository;
		this.consultaVendas = consultaVendas;
		this.auditoriaService = auditoriaService;
	}

	public Page<AnaliseDTO.ExecucaoResponse> listar(Pageable pageable, UsuarioAtual sessao) {
		Acesso.exigirLeituraAnalise(sessao);
		return execucaoRepository.findByEstabelecimentoId(sessao.estabelecimentoId(), pageable)
				.map(AnaliseDTO.ExecucaoResponse::from);
	}

	public AnaliseDTO.ExecucaoResponse buscar(Long id, UsuarioAtual sessao) {
		Acesso.exigirLeituraAnalise(sessao);
		return AnaliseDTO.ExecucaoResponse.from(carregar(id, sessao));
	}

	public Page<AnaliseDTO.ClassificacaoResponse> classificacoes(Long id, Pageable pageable, UsuarioAtual sessao) {
		Acesso.exigirLeituraAnalise(sessao);
		carregar(id, sessao);
		Set<String> fortes = antecedentesFortes(id);
		return classificacaoRepository.findByExecucaoId(id, pageable)
				.map(item -> AnaliseDTO.ClassificacaoResponse.from(item, texto(item, fortes)));
	}

	public Page<AnaliseDTO.RegraResponse> regras(Long id, Pageable pageable, UsuarioAtual sessao) {
		Acesso.exigirLeituraAnalise(sessao);
		carregar(id, sessao);
		return regraRepository.findByExecucaoId(id, pageable).map(AnaliseDTO.RegraResponse::from);
	}

	public List<AnaliseDTO.Combo> combos(Long id, UsuarioAtual sessao) {
		Acesso.exigirLeituraAnalise(sessao);
		carregar(id, sessao);
		Map<String, BigDecimal> margens = new HashMap<>();
		for (ResultadoClassificacao item : classificacaoRepository.findByExecucaoId(id, Sort.by("id"))) {
			margens.put(rotulo(item), item.getMargem());
		}
		List<AnaliseDTO.Combo> combos = new ArrayList<>();
		for (ResultadoRegra regra : regraRepository.findByExecucaoId(id, Sort.by(Sort.Direction.DESC, "lift"))) {
			BigDecimal esquerda = margens.get(regra.getAntecedente());
			BigDecimal direita = margens.get(regra.getConsequente());
			BigDecimal combinada = esquerda == null || direita == null ? null : esquerda.add(direita);
			combos.add(new AnaliseDTO.Combo(
					regra.getAntecedente(),
					regra.getConsequente(),
					regra.getLift(),
					combinada,
					regra.isFragil()));
		}
		return combos;
	}

	@Transactional
	public AnaliseDTO.ExecucaoResponse criar(AnaliseDTO.Create dto, UsuarioAtual sessao) {
		Acesso.exigirEscritaAnalise(sessao);
		Acesso.exigirMesmoEstabelecimento(dto.estabelecimentoId(), sessao);
		return executar(nova(dto.periodoIni(), dto.periodoFim(), dto.granularidade(), sessao), sessao);
	}

	@Transactional
	public AnaliseDTO.ExecucaoResponse reexecutar(Long id, UsuarioAtual sessao) {
		Acesso.exigirEscritaAnalise(sessao);
		AnaliseExecucao anterior = carregar(id, sessao);
		return executar(nova(anterior.getPeriodoIni(), anterior.getPeriodoFim(), anterior.getGranularidade(), sessao), sessao);
	}

	@Transactional
	public AnaliseDTO.SimulacaoResponse simular(AnaliseDTO.Simulacao dto, UsuarioAtual sessao) {
		Acesso.exigirEscritaAnalise(sessao);
		if (dto.periodoFim().isBefore(dto.periodoIni())) {
			throw new RequisicaoInvalidaException("Período inválido");
		}
		Produto produto = produtoRepository.findByIdAndEstabelecimentoId(dto.produtoId(), sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Produto", dto.produtoId()));
		CalculadoraCardapio.Saida saida = calcular(sessao.estabelecimentoId(), dto.periodoIni(), dto.periodoFim(),
				com.projeto.entity.enums.Granularidade.MES);
		CalculadoraCardapio.ItemClassificado atual = saida.itens().stream()
				.filter(item -> item.produtoId().equals(produto.getId()))
				.findFirst()
				.orElse(null);
		String aviso = "A simulação não altera o cardápio e mantém a popularidade do período.";
		if (atual == null) {
			return new AnaliseDTO.SimulacaoResponse(produto.getId(), produto.getNome(), produto.getPrecoAtual(),
					dto.precoNovo(), null, null, null, null, aviso + " O item não teve venda válida no período.");
		}
		if (atual.classe() == ClasseMenu.DADO_INCOMPLETO || atual.margem() == null) {
			return new AnaliseDTO.SimulacaoResponse(produto.getId(), produto.getNome(), produto.getPrecoAtual(),
					dto.precoNovo(), atual.margem(), null, atual.classe(), ClasseMenu.DADO_INCOMPLETO,
					aviso + " Sem custo, o item continua como dado incompleto.");
		}
		BigDecimal precoMedio = atual.quantidade() == 0
				? produto.getPrecoAtual()
				: atual.receita().divide(BigDecimal.valueOf(atual.quantidade()), 2, RoundingMode.HALF_UP);
		BigDecimal custo = precoMedio.subtract(atual.margem());
		BigDecimal margemNova = dto.precoNovo().subtract(custo).setScale(2, RoundingMode.HALF_UP);
		ClasseMenu classeNova = reclassificar(saida, atual, margemNova, sessao.estabelecimentoId());
		return new AnaliseDTO.SimulacaoResponse(
				produto.getId(),
				produto.getNome(),
				precoMedio,
				dto.precoNovo(),
				atual.margem(),
				margemNova,
				atual.classe(),
				classeNova,
				aviso);
	}

	public AnaliseDTO.Comparacao comparar(
			Instant inicioA,
			Instant fimA,
			Instant inicioB,
			Instant fimB,
			UsuarioAtual sessao) {
		Acesso.exigirLeituraAnalise(sessao);
		var a = calcular(sessao.estabelecimentoId(), inicioA, fimA, com.projeto.entity.enums.Granularidade.MES);
		var b = calcular(sessao.estabelecimentoId(), inicioB, fimB, com.projeto.entity.enums.Granularidade.MES);
		Map<Long, CalculadoraCardapio.ItemClassificado> mapaB = new HashMap<>();
		for (var item : b.itens()) {
			mapaB.put(item.produtoId(), item);
		}
		Set<Long> vistos = new HashSet<>();
		List<AnaliseDTO.MudancaClasse> mudancas = new ArrayList<>();
		for (var item : a.itens()) {
			vistos.add(item.produtoId());
			var outro = mapaB.get(item.produtoId());
			ClasseMenu classeB = outro == null ? null : outro.classe();
			if (item.classe() != classeB) {
				mudancas.add(new AnaliseDTO.MudancaClasse(item.produtoId(), item.nome(), item.classe(), classeB));
			}
		}
		for (var item : b.itens()) {
			if (vistos.contains(item.produtoId())) {
				continue;
			}
			mudancas.add(new AnaliseDTO.MudancaClasse(item.produtoId(), item.nome(), null, item.classe()));
		}
		return new AnaliseDTO.Comparacao(mudancas);
	}

	public List<ResultadoClassificacao> classificacoesCompletas(Long id, UsuarioAtual sessao) {
		Acesso.exigirLeituraAnalise(sessao);
		carregar(id, sessao);
		return classificacaoRepository.findByExecucaoId(id, Sort.by("classe"));
	}

	public List<ResultadoRegra> regrasCompletas(Long id, UsuarioAtual sessao) {
		Acesso.exigirLeituraAnalise(sessao);
		carregar(id, sessao);
		return regraRepository.findByExecucaoId(id, Sort.by(Sort.Direction.DESC, "lift"));
	}

	public byte[] exportarCsv(Long id, UsuarioAtual sessao) {
		return Exportador.csvAnalise(classificacoesCompletas(id, sessao), regrasCompletas(id, sessao));
	}

	public byte[] exportarPdf(Long id, UsuarioAtual sessao) {
		return Exportador.pdfAnalise(classificacoesCompletas(id, sessao), regrasCompletas(id, sessao));
	}

	private AnaliseDTO.ExecucaoResponse executar(AnaliseExecucao execucao, UsuarioAtual sessao) {
		execucao.setStatus(StatusAnalise.EM_EXECUCAO);
		execucao.setIniciadoEm(Instant.now());
		execucao = execucaoRepository.save(execucao);
		try {
			ConfigAnalise config = configAnaliseService.obterOuCriar(sessao.estabelecimentoId());
			CalculadoraCardapio.Saida saida = calcular(
					sessao.estabelecimentoId(),
					execucao.getPeriodoIni(),
					execucao.getPeriodoFim(),
					execucao.getGranularidade());
			Map<Long, Produto> produtos = new HashMap<>();
			for (Produto produto : produtoRepository.findByEstabelecimentoIdAndAtivoTrue(sessao.estabelecimentoId())) {
				produtos.put(produto.getId(), produto);
			}
			for (CalculadoraCardapio.ItemClassificado item : saida.itens()) {
				Produto produto = produtos.get(item.produtoId());
				if (produto == null) {
					produto = produtoRepository.findById(item.produtoId())
							.orElseThrow(() -> new RecursoNaoEncontradoException("Produto", item.produtoId()));
				}
				ResultadoClassificacao resultado = new ResultadoClassificacao();
				resultado.setExecucao(execucao);
				resultado.setProduto(produto);
				resultado.setPopularidade(item.popularidade());
				resultado.setMargem(item.margem());
				resultado.setClasse(item.classe());
				classificacaoRepository.save(resultado);
			}
			for (MotorMenu.RegraMinerada regra : saida.mineracao().regras()) {
				ResultadoRegra salvo = new ResultadoRegra();
				salvo.setExecucao(execucao);
				salvo.setAntecedente(regra.antecedente());
				salvo.setConsequente(regra.consequente());
				salvo.setSuporte(regra.suporte());
				salvo.setConfianca(regra.confianca());
				salvo.setLift(regra.lift());
				salvo.setFragil(regra.fragil());
				regraRepository.save(salvo);
			}
			execucao.setParametros(parametros(config));
			execucao.setVersaoDados("itens:" + saida.itens().size());
			execucao.setStatus(StatusAnalise.CONCLUIDA);
			execucao.setConcluidoEm(Instant.now());
			execucao.setErro(saida.mineracao().baseInsuficiente()
					? "Base insuficiente para associação (mínimo de 200 pedidos). A matriz foi calculada."
					: null);
			execucaoRepository.saveAndFlush(execucao);
			atualizarAlertas(execucao, config);
			auditoriaService.registrar(sessao, "ANALISAR", "analise_execucao", execucao.getId(), null, execucao.getStatus().name());
			return AnaliseDTO.ExecucaoResponse.from(execucao);
		} catch (RuntimeException ex) {
			execucao.setStatus(StatusAnalise.FALHOU);
			execucao.setConcluidoEm(Instant.now());
			execucao.setErro(ex.getMessage() == null ? "Falha na análise" : ex.getMessage());
			execucaoRepository.save(execucao);
			return AnaliseDTO.ExecucaoResponse.from(execucao);
		}
	}

	private void atualizarAlertas(AnaliseExecucao execucao, ConfigAnalise config) {
		for (ResultadoClassificacao item : classificacaoRepository.findByExecucaoId(execucao.getId(), Sort.by("id"))) {
			List<ClasseMenu> historico = classificacaoRepository.classesRecentes(
					item.getProduto().getId(),
					execucao.getGranularidade(),
					execucao.getEstabelecimento().getId());
			int seguidos = MotorMenu.consecutivosCao(historico);
			Alerta alerta = alertaRepository.findByProdutoIdAndTipo(item.getProduto().getId(), TipoAlerta.CAO_RECORRENTE)
					.orElseGet(Alerta::new);
			if (alerta.getId() == null && seguidos < config.getNAlertaCao()) {
				continue;
			}
			alerta.setProduto(item.getProduto());
			alerta.setTipo(TipoAlerta.CAO_RECORRENTE);
			alerta.setPeriodos(seguidos);
			alerta.setAtivo(seguidos >= config.getNAlertaCao());
			alertaRepository.save(alerta);
		}
	}

	private ClasseMenu reclassificar(
			CalculadoraCardapio.Saida saida,
			CalculadoraCardapio.ItemClassificado atual,
			BigDecimal margemNova,
			Long estabelecimentoId) {
		ConfigAnalise config = configAnaliseService.obterOuCriar(estabelecimentoId);
		List<CalculadoraCardapio.ItemClassificado> grupo = new ArrayList<>();
		for (var item : saida.itens()) {
			if (item.classe() == ClasseMenu.DADO_INCOMPLETO || item.classe() == ClasseMenu.EM_OBSERVACAO) {
				continue;
			}
			if (config.isEscopoPorCategoria() && !java.util.Objects.equals(item.categoriaId(), atual.categoriaId())) {
				continue;
			}
			grupo.add(item.produtoId().equals(atual.produtoId())
					? new CalculadoraCardapio.ItemClassificado(
							item.produtoId(), item.nome(), item.rotulo(), item.categoriaId(), item.categoriaNome(),
							item.quantidade(), item.receita(), item.popularidade(), margemNova, item.classe())
					: item);
		}
		if (grupo.size() < 3 && config.isEscopoPorCategoria()) {
			grupo.clear();
			for (var item : saida.itens()) {
				if (item.classe() == ClasseMenu.DADO_INCOMPLETO || item.classe() == ClasseMenu.EM_OBSERVACAO) {
					continue;
				}
				grupo.add(item.produtoId().equals(atual.produtoId())
						? new CalculadoraCardapio.ItemClassificado(
								item.produtoId(), item.nome(), item.rotulo(), item.categoriaId(), item.categoriaNome(),
								item.quantidade(), item.receita(), item.popularidade(), margemNova, item.classe())
						: item);
			}
		}
		List<BigDecimal> margens = grupo.stream().map(CalculadoraCardapio.ItemClassificado::margem).toList();
		List<Long> quantidades = grupo.stream().map(CalculadoraCardapio.ItemClassificado::quantidade).toList();
		BigDecimal media = config.getCriterioPopularidade() == CriterioPopularidade.SIMPLES
				? MotorMenu.mediaSimples(margens)
				: MotorMenu.mediaPonderada(margens, quantidades);
		boolean alta = MotorMenu.margemAlta(margemNova, media);
		boolean itemPopular = MotorMenu.popular(atual.popularidade(), Math.max(grupo.size(), 1));
		return MotorMenu.classe(itemPopular, alta);
	}

	private CalculadoraCardapio.Saida calcular(
			Long estabelecimentoId,
			Instant inicio,
			Instant fim,
			com.projeto.entity.enums.Granularidade granularidade) {
		if (inicio == null || fim == null || fim.isBefore(inicio)) {
			throw new RequisicaoInvalidaException("Período inválido");
		}
		ConfigAnalise config = configAnaliseService.obterOuCriar(estabelecimentoId);
		return CalculadoraCardapio.calcular(
				consultaVendas.linhas(estabelecimentoId, inicio, fim),
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

	private AnaliseExecucao nova(Instant inicio, Instant fim, com.projeto.entity.enums.Granularidade granularidade, UsuarioAtual sessao) {
		if (inicio == null || fim == null || !fim.isAfter(inicio)) {
			throw new RequisicaoInvalidaException("Período inválido");
		}
		Estabelecimento estabelecimento = estabelecimentoRepository.findById(sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento", sessao.estabelecimentoId()));
		AnaliseExecucao execucao = new AnaliseExecucao();
		execucao.setEstabelecimento(estabelecimento);
		execucao.setPeriodoIni(inicio);
		execucao.setPeriodoFim(fim);
		execucao.setGranularidade(granularidade);
		execucao.setStatus(StatusAnalise.PENDENTE);
		return execucao;
	}

	private AnaliseExecucao carregar(Long id, UsuarioAtual sessao) {
		return execucaoRepository.findByIdAndEstabelecimentoId(id, sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Análise", id));
	}

	private Set<String> antecedentesFortes(Long execucaoId) {
		Set<String> fortes = new HashSet<>();
		for (ResultadoRegra regra : regraRepository.findByExecucaoId(execucaoId, Sort.by(Sort.Direction.DESC, "lift"))) {
			if (!regra.isFragil()) {
				fortes.add(regra.getAntecedente());
			}
		}
		return fortes;
	}

	private String texto(ResultadoClassificacao item, Set<String> fortes) {
		return MotorMenu.recomendacao(item.getClasse(), fortes.contains(rotulo(item)));
	}

	private String rotulo(ResultadoClassificacao item) {
		return item.getProduto().getNome() + " (#" + item.getProduto().getId() + ")";
	}

	private String parametros(ConfigAnalise config) {
		return String.format(Locale.US,
				"{\"suporteMin\":%s,\"confiancaMin\":%s,\"liftMin\":%s,\"periodoMinimoDias\":%d,\"nAlertaCao\":%d,\"criterio\":\"%s\",\"escopoPorCategoria\":%s}",
				config.getSuporteMin().toPlainString(),
				config.getConfiancaMin().toPlainString(),
				config.getLiftMin().toPlainString(),
				config.getPeriodoMinimoDias(),
				config.getNAlertaCao(),
				config.getCriterioPopularidade().name(),
				config.isEscopoPorCategoria());
	}
}
