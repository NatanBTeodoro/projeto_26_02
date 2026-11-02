package com.projeto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.projeto.dto.ProdutoDTO;
import com.projeto.entity.Estabelecimento;
import com.projeto.entity.PrecoCustoHistorico;
import com.projeto.entity.Produto;
import com.projeto.entity.enums.Perfil;
import com.projeto.repository.CategoriaRepository;
import com.projeto.repository.EstabelecimentoRepository;
import com.projeto.repository.PrecoCustoHistoricoRepository;
import com.projeto.repository.ProdutoRepository;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.util.exception.RequisicaoInvalidaException;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

	@Mock
	private ProdutoRepository produtoRepository;
	@Mock
	private CategoriaRepository categoriaRepository;
	@Mock
	private EstabelecimentoRepository estabelecimentoRepository;
	@Mock
	private PrecoCustoHistoricoRepository historicoRepository;

	private ProdutoService service;
	private UsuarioAtual gestor;

	@BeforeEach
	void preparar() {
		service = new ProdutoService(produtoRepository, categoriaRepository, estabelecimentoRepository, historicoRepository);
		gestor = new UsuarioAtual(2L, 1L, Perfil.GESTOR, "gestor@casa.test");
	}

	@Test
	@DisplayName("Cadastro grava o snapshot inicial de preço e custo")
	void criaComHistorico() {
		Estabelecimento estabelecimento = new Estabelecimento();
		estabelecimento.setId(1L);
		when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(estabelecimento));
		when(produtoRepository.save(any(Produto.class))).thenAnswer(invocacao -> {
			Produto produto = invocacao.getArgument(0);
			produto.setId(15L);
			return produto;
		});
		when(historicoRepository.findFirstByProdutoIdAndVigenciaFimIsNullOrderByVigenciaIniDesc(15L))
				.thenReturn(Optional.empty());

		ProdutoDTO.Response resposta = service.criar(
				new ProdutoDTO.Create(1L, null, "Pizza", new BigDecimal("40.00"), new BigDecimal("12.00"), false, null, null),
				gestor);

		assertEquals(new BigDecimal("40.00"), resposta.precoAtual());
		ArgumentCaptor<PrecoCustoHistorico> historico = ArgumentCaptor.forClass(PrecoCustoHistorico.class);
		verify(historicoRepository).save(historico.capture());
		assertEquals(new BigDecimal("12.00"), historico.getValue().getCusto());
		assertEquals(15L, historico.getValue().getProduto().getId());
	}

	@Test
	@DisplayName("Atualizar sem mudar preço não abre nova vigência")
	void atualizaSemNovaVigencia() {
		Produto produto = new Produto();
		produto.setId(15L);
		Estabelecimento estabelecimento = new Estabelecimento();
		estabelecimento.setId(1L);
		produto.setEstabelecimento(estabelecimento);
		produto.setNome("Pizza");
		produto.setPrecoAtual(new BigDecimal("40.00"));
		produto.setCustoAtual(new BigDecimal("12.00"));
		produto.setAtivo(true);
		when(produtoRepository.findByIdAndEstabelecimentoId(15L, 1L)).thenReturn(Optional.of(produto));
		when(produtoRepository.save(produto)).thenReturn(produto);

		service.atualizar(15L, new ProdutoDTO.Update(null, "Pizza grande", new BigDecimal("40.00"), new BigDecimal("12.00"),
				false, null, null, true), gestor);

		verify(historicoRepository, never()).save(any());
	}

	@Test
	@DisplayName("Janela sazonal invertida é recusada e nada é salvo")
	void janelaInvalida() {
		Estabelecimento estabelecimento = new Estabelecimento();
		estabelecimento.setId(1L);
		when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(estabelecimento));

		assertThrows(RequisicaoInvalidaException.class, () -> service.criar(new ProdutoDTO.Create(
				1L, null, "Sopa", new BigDecimal("10.00"), null, true,
				java.time.LocalDate.of(2026, 8, 1), java.time.LocalDate.of(2026, 7, 1)), gestor));
		verify(produtoRepository, never()).save(any());
	}
}
