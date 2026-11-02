package com.projeto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.projeto.dto.CategoriaDTO;
import com.projeto.entity.Categoria;
import com.projeto.entity.Estabelecimento;
import com.projeto.entity.enums.Perfil;
import com.projeto.repository.CategoriaRepository;
import com.projeto.repository.EstabelecimentoRepository;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.util.exception.OperacaoNaoPermitidaException;
import com.projeto.util.exception.RecursoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

	@Mock
	private CategoriaRepository categoriaRepository;
	@Mock
	private EstabelecimentoRepository estabelecimentoRepository;

	private CategoriaService service;
	private UsuarioAtual gestor;

	@BeforeEach
	void preparar() {
		service = new CategoriaService(categoriaRepository, estabelecimentoRepository);
		gestor = new UsuarioAtual(2L, 1L, Perfil.GESTOR, "gestor@casa.test");
	}

	@Test
	@DisplayName("Gestor cadastra categoria no próprio estabelecimento")
	void criaCategoria() {
		Estabelecimento estabelecimento = estabelecimento(1L);
		when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(estabelecimento));
		when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocacao -> {
			Categoria categoria = invocacao.getArgument(0);
			categoria.setId(8L);
			return categoria;
		});

		CategoriaDTO.Response resposta = service.criar(new CategoriaDTO.Create(1L, " Bebidas "), gestor);

		assertEquals(8L, resposta.id());
		assertEquals("Bebidas", resposta.nome());
		assertEquals(1L, resposta.estabelecimentoId());
		verify(categoriaRepository).save(any(Categoria.class));
	}

	@Test
	@DisplayName("Categoria de outro estabelecimento é recusada")
	void outroEstabelecimento() {
		assertThrows(OperacaoNaoPermitidaException.class,
				() -> service.criar(new CategoriaDTO.Create(99L, "Bebidas"), gestor));
	}

	@Test
	@DisplayName("Analista não altera categoria")
	void analistaNaoCria() {
		UsuarioAtual analista = new UsuarioAtual(3L, 1L, Perfil.ANALISTA, "a@casa.test");
		assertThrows(OperacaoNaoPermitidaException.class,
				() -> service.criar(new CategoriaDTO.Create(1L, "Bebidas"), analista));
	}

	@Test
	@DisplayName("Busca de categoria de outro tenant responde como inexistente")
	void buscaIsolada() {
		when(categoriaRepository.findByIdAndEstabelecimentoId(4L, 1L)).thenReturn(Optional.empty());
		assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(4L, gestor));
	}

	private Estabelecimento estabelecimento(Long id) {
		Estabelecimento estabelecimento = new Estabelecimento();
		estabelecimento.setId(id);
		estabelecimento.setNome("Casa");
		estabelecimento.setAtivo(true);
		return estabelecimento;
	}
}
