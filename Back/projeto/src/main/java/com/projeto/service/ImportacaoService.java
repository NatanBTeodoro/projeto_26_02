package com.projeto.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.projeto.service.AuditoriaService;
import com.projeto.dto.ImportacaoDTO;
import com.projeto.entity.Estabelecimento;
import com.projeto.entity.Importacao;
import com.projeto.entity.Pedido;
import com.projeto.entity.PedidoItem;
import com.projeto.entity.Produto;
import com.projeto.entity.enums.StatusImportacao;
import com.projeto.entity.enums.StatusPedido;
import com.projeto.repository.EstabelecimentoRepository;
import com.projeto.repository.ImportacaoRepository;
import com.projeto.repository.PedidoRepository;
import com.projeto.repository.ProdutoRepository;
import com.projeto.seguranca.Acesso;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.util.Pseudonimo;
import com.projeto.util.exception.RecursoNaoEncontradoException;
import com.projeto.util.exception.RequisicaoInvalidaException;

@Service
@Transactional(readOnly = true)
public class ImportacaoService {

	private static final int MAX_LINHAS = 20000;

	private final ImportacaoRepository importacaoRepository;
	private final PedidoRepository pedidoRepository;
	private final ProdutoRepository produtoRepository;
	private final EstabelecimentoRepository estabelecimentoRepository;
	private final AuditoriaService auditoriaService;
	private final String pepper;

	public ImportacaoService(
			ImportacaoRepository importacaoRepository,
			PedidoRepository pedidoRepository,
			ProdutoRepository produtoRepository,
			EstabelecimentoRepository estabelecimentoRepository,
			AuditoriaService auditoriaService,
			@Value("${app.pepper:quadrante-dev-pepper}") String pepper) {
		this.importacaoRepository = importacaoRepository;
		this.pedidoRepository = pedidoRepository;
		this.produtoRepository = produtoRepository;
		this.estabelecimentoRepository = estabelecimentoRepository;
		this.auditoriaService = auditoriaService;
		this.pepper = pepper;
	}

	public Page<ImportacaoDTO.Response> listar(Pageable pageable, UsuarioAtual sessao) {
		Acesso.exigirVendas(sessao);
		return importacaoRepository.findByEstabelecimentoId(sessao.estabelecimentoId(), pageable)
				.map(ImportacaoDTO.Response::from);
	}

	public ImportacaoDTO.Response buscar(Long id, UsuarioAtual sessao) {
		Acesso.exigirVendas(sessao);
		return ImportacaoDTO.Response.from(carregar(id, sessao));
	}

	public ImportacaoDTO.Previa previsualizar(MultipartFile arquivo, Long estabelecimentoId, UsuarioAtual sessao) {
		Acesso.exigirVendas(sessao);
		Acesso.exigirMesmoEstabelecimento(estabelecimentoId, sessao);
		Leitura leitura = ler(arquivo, sessao);
		return new ImportacaoDTO.Previa(
				nome(arquivo),
				leitura.separador(),
				contagemValida(leitura),
				leitura.erros().size(),
				leitura.ignoradas(),
				leitura.erros(),
				leitura.amostra());
	}

	@Transactional
	public ImportacaoDTO.Response confirmar(MultipartFile arquivo, Long estabelecimentoId, UsuarioAtual sessao) {
		Acesso.exigirVendas(sessao);
		Acesso.exigirMesmoEstabelecimento(estabelecimentoId, sessao);
		Leitura leitura = ler(arquivo, sessao);
		Estabelecimento estabelecimento = estabelecimentoRepository.findById(sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento", sessao.estabelecimentoId()));
		int gravados = 0;
		for (Grupo grupo : leitura.grupos()) {
			if (grupo.linhas().isEmpty()) {
				continue;
			}
			if (grupo.externalId() != null
					&& pedidoRepository.existsByEstabelecimentoIdAndExternalId(sessao.estabelecimentoId(), grupo.externalId())) {
				continue;
			}
			Pedido pedido = new Pedido();
			pedido.setEstabelecimento(estabelecimento);
			pedido.setExternalId(grupo.externalId());
			pedido.setDataHora(grupo.dataHora());
			pedido.setStatus(StatusPedido.ATIVO);
			pedido.setClienteHash(grupo.clienteHash());
			for (LinhaValida linha : grupo.linhas()) {
				PedidoItem item = new PedidoItem();
				item.setPedido(pedido);
				item.setProduto(linha.produto());
				item.setQuantidade(linha.quantidade());
				item.setPrecoUnitario(linha.produto().getPrecoAtual());
				item.setCustoUnitario(linha.produto().getCustoAtual());
				pedido.getItens().add(item);
			}
			pedidoRepository.save(pedido);
			gravados++;
		}
		Importacao importacao = new Importacao();
		importacao.setEstabelecimento(estabelecimento);
		importacao.setArquivo(nome(arquivo));
		importacao.setLinhasOk(contagemValida(leitura));
		importacao.setLinhasErro(leitura.erros().size());
		importacao.setRelatorioErros(relatorio(leitura));
		importacao.setStatus(leitura.erros().isEmpty() ? StatusImportacao.CONCLUIDA : StatusImportacao.CONCLUIDA_COM_ERROS);
		Importacao salva = importacaoRepository.save(importacao);
		auditoriaService.registrar(sessao, "IMPORTAR", "importacao", salva.getId(), null, gravados + " pedidos");
		return ImportacaoDTO.Response.from(salva);
	}

	private Leitura ler(MultipartFile arquivo, UsuarioAtual sessao) {
		if (arquivo == null || arquivo.isEmpty()) {
			throw new RequisicaoInvalidaException("Arquivo CSV vazio");
		}
		String nome = nome(arquivo).toLowerCase(Locale.ROOT);
		if (!nome.endsWith(".csv") && !nome.endsWith(".txt")) {
			throw new RequisicaoInvalidaException("Envie um arquivo CSV");
		}
		try {
			String texto = new String(arquivo.getBytes(), StandardCharsets.UTF_8);
			if (texto.startsWith("\uFEFF")) {
				texto = texto.substring(1);
			}
			char separador = texto.contains(";") && contar(texto, ';') >= contar(texto, ',') ? ';' : ',';
			CSVFormat formato = CSVFormat.DEFAULT.builder()
					.setDelimiter(separador)
					.setHeader()
					.setSkipHeaderRecord(true)
					.setIgnoreHeaderCase(true)
					.setTrim(true)
					.setIgnoreEmptyLines(true)
					.build();
			List<String> erros = new ArrayList<>();
			List<String> amostra = new ArrayList<>();
			List<LinhaValida> validas = new ArrayList<>();
			int ignoradas = 0;
			Map<Long, Produto> cache = new LinkedHashMap<>();
			try (CSVParser parser = CSVParser.parse(texto, formato)) {
				for (CSVRecord registro : parser) {
					if (parser.getRecordNumber() > MAX_LINHAS) {
						throw new RequisicaoInvalidaException("CSV acima de " + MAX_LINHAS + " linhas");
					}
					long numero = registro.getRecordNumber() + 1;
					try {
						LinhaValida linha = interpretar(registro, numero, sessao, cache);
						if (linha.ignorada()) {
							ignoradas++;
							continue;
						}
						validas.add(linha);
						if (amostra.size() < 5) {
							amostra.add("linha " + numero + ": produto " + linha.produto().getId() + " x " + linha.quantidade());
						}
					} catch (RequisicaoInvalidaException ex) {
						erros.add("linha " + numero + ": " + ex.getMessage());
					}
				}
			}
			return new Leitura(String.valueOf(separador), validas, erros, amostra, ignoradas, agrupar(validas));
		} catch (IOException ex) {
			throw new RequisicaoInvalidaException("Não foi possível ler o CSV");
		} catch (IllegalArgumentException ex) {
			throw new RequisicaoInvalidaException("Cabeçalho do CSV inválido. Use external_id,data_hora,produto_id,quantidade");
		}
	}

	private LinhaValida interpretar(CSVRecord registro, long numero, UsuarioAtual sessao, Map<Long, Produto> cache) {
		String external = valor(registro, "external_id", "externalid", "id_externo");
		String dataTexto = valor(registro, "data_hora", "datahora", "data");
		String produtoTexto = valor(registro, "produto_id", "produtoid", "produto");
		String quantidadeTexto = valor(registro, "quantidade", "qtd");
		String cliente = valor(registro, "cliente", "cliente_codigo");
		if (produtoTexto == null || quantidadeTexto == null) {
			throw new RequisicaoInvalidaException("produto_id e quantidade são obrigatórios");
		}
		long produtoId;
		int quantidade;
		try {
			produtoId = Long.parseLong(produtoTexto.trim());
			quantidade = Integer.parseInt(quantidadeTexto.trim());
		} catch (NumberFormatException ex) {
			throw new RequisicaoInvalidaException("produto_id e quantidade precisam ser números inteiros");
		}
		if (quantidade < 1) {
			throw new RequisicaoInvalidaException("quantidade precisa ser maior que zero");
		}
		Instant data = lerData(dataTexto);
		if (data.isAfter(Instant.now().plusSeconds(60))) {
			throw new RequisicaoInvalidaException("data futura");
		}
		Produto produto = cache.computeIfAbsent(produtoId, id -> produtoRepository
				.findByIdAndEstabelecimentoId(id, sessao.estabelecimentoId())
				.orElse(null));
		if (produto == null) {
			throw new RequisicaoInvalidaException("produto não encontrado neste estabelecimento");
		}
		if (!produto.isAtivo()) {
			throw new RequisicaoInvalidaException("produto inativo");
		}
		String externalId = external == null || external.isBlank() ? null : external.trim();
		boolean ignorada = externalId != null
				&& pedidoRepository.existsByEstabelecimentoIdAndExternalId(sessao.estabelecimentoId(), externalId);
		return new LinhaValida(numero, externalId, data, produto, quantidade, Pseudonimo.hmac(cliente, pepper), ignorada);
	}

	private List<Grupo> agrupar(List<LinhaValida> validas) {
		Map<String, Grupo> grupos = new LinkedHashMap<>();
		int avulsos = 0;
		for (LinhaValida linha : validas) {
			if (linha.ignorada()) {
				continue;
			}
			String chave = linha.externalId() != null ? linha.externalId() : "linha-" + (++avulsos);
			grupos.computeIfAbsent(chave, id -> new Grupo(linha.externalId(), linha.dataHora(), linha.clienteHash(), new ArrayList<>()))
					.linhas()
					.add(linha);
		}
		return new ArrayList<>(grupos.values());
	}

	private Instant lerData(String texto) {
		if (texto == null || texto.isBlank()) {
			return Instant.now();
		}
		String valor = texto.trim();
		try {
			return Instant.parse(valor);
		} catch (DateTimeParseException ignorado) {
			try {
				return LocalDate.parse(valor).atStartOfDay().toInstant(ZoneOffset.UTC);
			} catch (DateTimeParseException ex) {
				throw new RequisicaoInvalidaException("data_hora inválida");
			}
		}
	}

	private String valor(CSVRecord registro, String... nomes) {
		for (String nome : nomes) {
			if (registro.isMapped(nome) && registro.get(nome) != null && !registro.get(nome).isBlank()) {
				return registro.get(nome).trim();
			}
		}
		return null;
	}

	private int contagemValida(Leitura leitura) {
		return (int) leitura.validas().stream().filter(linha -> !linha.ignorada()).count();
	}

	private String relatorio(Leitura leitura) {
		StringBuilder texto = new StringBuilder();
		if (leitura.ignoradas() > 0) {
			texto.append("ignoradas por identificador externo já existente: ").append(leitura.ignoradas()).append('\n');
		}
		for (String erro : leitura.erros()) {
			texto.append(erro).append('\n');
		}
		return texto.isEmpty() ? null : texto.toString();
	}

	private Importacao carregar(Long id, UsuarioAtual sessao) {
		return importacaoRepository.findByIdAndEstabelecimentoId(id, sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Importação", id));
	}

	private String nome(MultipartFile arquivo) {
		String original = arquivo.getOriginalFilename();
		if (original == null || original.isBlank()) {
			return "vendas.csv";
		}
		return original.length() > 255 ? original.substring(original.length() - 255) : original;
	}

	private int contar(String texto, char alvo) {
		int total = 0;
		for (int i = 0; i < texto.length(); i++) {
			if (texto.charAt(i) == alvo) {
				total++;
			}
		}
		return total;
	}

	private record LinhaValida(
			long numero,
			String externalId,
			Instant dataHora,
			Produto produto,
			int quantidade,
			String clienteHash,
			boolean ignorada) {
	}

	private record Grupo(String externalId, Instant dataHora, String clienteHash, List<LinhaValida> linhas) {
	}

	private record Leitura(
			String separador,
			List<LinhaValida> validas,
			List<String> erros,
			List<String> amostra,
			int ignoradas,
			List<Grupo> grupos) {
	}
}
