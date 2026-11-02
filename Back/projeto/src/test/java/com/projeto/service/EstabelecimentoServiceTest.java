package com.projeto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.projeto.entity.Estabelecimento;
import com.projeto.entity.enums.Perfil;
import com.projeto.repository.EstabelecimentoRepository;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.util.exception.OperacaoNaoPermitidaException;
import com.projeto.util.exception.RecursoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class EstabelecimentoServiceTest {

	@Mock
	private EstabelecimentoRepository estabelecimentoRepository;

	private EstabelecimentoService service;

	@BeforeEach
	void preparar() {
		service = new EstabelecimentoService(estabelecimentoRepository);
	}

	@Test
	@DisplayName("A listagem devolve só o estabelecimento da sessão")
	void listaSomenteOProprio() {
		Estabelecimento estabelecimento = new Estabelecimento();
		estabelecimento.setId(1L);
		estabelecimento.setNome("Casa piloto");
		estabelecimento.setAtivo(true);
		when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(estabelecimento));
		UsuarioAtual gestor = new UsuarioAtual(2L, 1L, Perfil.GESTOR, "g@casa.test");

		var pagina = service.listar(PageRequest.of(0, 20), gestor);

		assertEquals(1, pagina.getTotalElements());
		assertEquals("Casa piloto", pagina.getContent().get(0).nome());
	}

	@Test
	@DisplayName("Gestor não cadastra outro estabelecimento")
	void gestorNaoCria() {
		UsuarioAtual gestor = new UsuarioAtual(2L, 1L, Perfil.GESTOR, "g@casa.test");
		assertThrows(OperacaoNaoPermitidaException.class,
				() -> service.criar(new com.projeto.dto.EstabelecimentoDTO.Create("Outra casa"), gestor));
	}

	@Test
	@DisplayName("Consulta de outro id não revela o registro")
	void outroIdSome() {
		UsuarioAtual gestor = new UsuarioAtual(2L, 1L, Perfil.GESTOR, "g@casa.test");
		assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(9L, gestor));
	}
}
