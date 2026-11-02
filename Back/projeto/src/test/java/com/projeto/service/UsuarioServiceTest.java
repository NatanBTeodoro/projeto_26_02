package com.projeto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.projeto.dto.UsuarioDTO;
import com.projeto.entity.Estabelecimento;
import com.projeto.entity.Usuario;
import com.projeto.entity.enums.Perfil;
import com.projeto.repository.EstabelecimentoRepository;
import com.projeto.repository.UsuarioRepository;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.util.exception.RecursoJaExistenteException;
import com.projeto.util.exception.RequisicaoInvalidaException;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;
	@Mock
	private EstabelecimentoRepository estabelecimentoRepository;
	@Mock
	private PasswordEncoder passwordEncoder;

	private UsuarioService service;
	private UsuarioAtual admin;

	@BeforeEach
	void preparar() {
		service = new UsuarioService(usuarioRepository, estabelecimentoRepository, passwordEncoder);
		admin = new UsuarioAtual(1L, 1L, Perfil.ADMINISTRADOR, "admin@quadrante.local");
	}

	@Test
	@DisplayName("Cadastro guarda hash diferente da senha e não devolve a senha")
	void criaComHash() {
		Estabelecimento estabelecimento = new Estabelecimento();
		estabelecimento.setId(1L);
		when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(estabelecimento));
		when(usuarioRepository.existsByEmailIgnoreCase("caixa@casa.test")).thenReturn(false);
		when(passwordEncoder.encode("Caixa123")).thenReturn("$2a$12$hash");
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> {
			Usuario usuario = invocacao.getArgument(0);
			usuario.setId(4L);
			return usuario;
		});

		UsuarioDTO.Response resposta = service.criar(
				new UsuarioDTO.Create(1L, " Caixa@Casa.Test ", "Caixa123", Perfil.OPERADOR), admin);

		assertEquals("caixa@casa.test", resposta.email());
		assertEquals(Perfil.OPERADOR, resposta.perfil());
		assertNotEquals("Caixa123", "$2a$12$hash");
	}

	@Test
	@DisplayName("E-mail duplicado não cria outro usuário")
	void emailDuplicado() {
		when(usuarioRepository.existsByEmailIgnoreCase("caixa@casa.test")).thenReturn(true);
		assertThrows(RecursoJaExistenteException.class, () -> service.criar(
				new UsuarioDTO.Create(1L, "caixa@casa.test", "Caixa123", Perfil.OPERADOR), admin));
	}

	@Test
	@DisplayName("Senha sem número é recusada")
	void senhaFraca() {
		assertThrows(RequisicaoInvalidaException.class, () -> service.criar(
				new UsuarioDTO.Create(1L, "caixa@casa.test", "somenteletras", Perfil.OPERADOR), admin));
	}
}
