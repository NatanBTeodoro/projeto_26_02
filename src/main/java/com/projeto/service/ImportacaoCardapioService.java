package com.projeto.service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.projeto.dto.ImportacaoCardapioDTO;
import com.projeto.entity.Categoria;
import com.projeto.entity.Produto;
import com.projeto.repository.CategoriaRepository;
import com.projeto.repository.ProdutoRepository;
import com.projeto.util.exception.RegraDeNegocioException;

@Service
public class ImportacaoCardapioService {

	private final CategoriaRepository categoriaRepository;
	private final ProdutoRepository produtoRepository;

	public ImportacaoCardapioService(CategoriaRepository categoriaRepository, ProdutoRepository produtoRepository) {
		this.categoriaRepository = categoriaRepository;
		this.produtoRepository = produtoRepository;
	}

	public byte[] modelo() {
		return LeitorPlanilhaCardapio.modeloCsv();
	}

	@Transactional(readOnly = true)
	public ImportacaoCardapioDTO.Previa prever(MultipartFile arquivo) {
		return previa(arquivo);
	}

	@Transactional
	public ImportacaoCardapioDTO.Resultado confirmar(MultipartFile arquivo, ImportacaoCardapioDTO.Modo modo) {
		ImportacaoCardapioDTO.Previa previa = previa(arquivo);
		if (!ImportacaoCardapio.deveAplicar(modo, previa.resumo().erros())) {
			return new ImportacaoCardapioDTO.Resultado(false, 0, previa.linhas(), previa.resumo());
		}
		Map<String, Categoria> categorias = new LinkedHashMap<>();
		for (Categoria categoria : categoriaRepository.findAll()) {
			categorias.putIfAbsent(chave(categoria.getNome()), categoria);
		}
		Map<String, Produto> produtos = new LinkedHashMap<>();
		for (Produto produto : produtoRepository.findAll()) {
			produtos.putIfAbsent(chave(produto.getCategoria().getNome()) + "|" + chave(produto.getNome()), produto);
		}

		int gravados = 0;
		for (ImportacaoCardapioDTO.Linha linha : previa.linhas()) {
			if (linha.situacao() == ImportacaoCardapioDTO.Situacao.ERRO) {
				continue;
			}
			Categoria categoria = categorias.get(chave(linha.categoria()));
			if (categoria == null) {
				categoria = new Categoria();
				categoria.setNome(linha.categoria());
				categoria.setAtiva(true);
				categoria = categoriaRepository.save(categoria);
				categorias.put(chave(categoria.getNome()), categoria);
			} else if (!categoria.isAtiva()) {
				categoria.setAtiva(true);
			}
			String chaveProduto = chave(linha.categoria()) + "|" + chave(linha.nome());
			Produto produto = produtos.get(chaveProduto);
			if (produto == null) {
				produto = new Produto();
				produto.setCategoria(categoria);
				produto.setNome(linha.nome());
				produto.setAtivo(true);
				produtos.put(chaveProduto, produto);
			}
			produto.setPrecoAtual(linha.preco());
			produto.setCustoAtual(linha.custo());
			produtoRepository.save(produto);
			gravados++;
		}
		return new ImportacaoCardapioDTO.Resultado(true, gravados, previa.linhas(), previa.resumo());
	}

	private ImportacaoCardapioDTO.Previa previa(MultipartFile arquivo) {
		if (arquivo == null || arquivo.isEmpty()) {
			throw new RegraDeNegocioException("Envie um arquivo .csv ou .xlsx");
		}
		try {
			LeitorPlanilhaCardapio.Leitura leitura = LeitorPlanilhaCardapio.ler(arquivo.getOriginalFilename(), arquivo.getBytes());
			if (leitura.erro() != null) {
				throw new RegraDeNegocioException(leitura.erro());
			}
			return ImportacaoCardapio.prever(
					leitura.linhas(),
					categoriaRepository.findAll().stream()
							.map(categoria -> new ImportacaoCardapio.CategoriaConhecida(categoria.getNome(), categoria.isAtiva()))
							.toList(),
					produtoRepository.findAll().stream()
							.map(produto -> new ImportacaoCardapio.ProdutoConhecido(
									produto.getCategoria().getNome(),
									produto.getNome(),
									produto.getPrecoAtual(),
									produto.getCustoAtual(),
									produto.isAtivo()))
							.toList());
		} catch (RegraDeNegocioException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RegraDeNegocioException("Não foi possível ler o arquivo");
		}
	}

	private static String chave(String texto) {
		return texto == null ? "" : texto.trim().toLowerCase(Locale.forLanguageTag("pt-BR"));
	}
}
