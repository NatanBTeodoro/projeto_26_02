package com.projeto.service;

import com.projeto.dto.LoginRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import com.projeto.entity.Estabelecimento;
import com.projeto.entity.Usuario;
import com.projeto.entity.enums.Perfil;
import com.projeto.seguranca.ContadorTentativasLogin;
import com.projeto.seguranca.SessaoAtual;
import com.projeto.seguranca.UsuarioDetails;
import com.projeto.util.exception.LimiteDeTentativasException;
import com.projeto.util.exception.NaoAutenticadoException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private SecurityContextRepository securityContextRepository;
	@Mock
	private ContadorTentativasLogin contador;
	@Mock
	private SessaoAtual sessaoAtual;

	@AfterEach
	void limpar() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("Senha errada devolve a mesma falha e registra a tentativa")
	void senhaErrada() {
		AuthService service = new AuthService(authenticationManager, securityContextRepository, contador, sessaoAtual);
		when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any()))
				.thenThrow(new BadCredentialsException("ruim"));

		NaoAutenticadoException erro = assertThrows(NaoAutenticadoException.class, () -> service.entrar(
				new LoginRequest("Admin@Quadrante.local", "errada1"),
				new MockHttpServletRequest(),
				new MockHttpServletResponse()));

		assertEquals("Usuário ou senha inválidos.", erro.getMessage());
		verify(contador).registrarFalha("admin@quadrante.local");
	}

	@Test
	@DisplayName("Conta bloqueada nem chega a conferir a senha")
	void bloqueado() {
		AuthService service = new AuthService(authenticationManager, securityContextRepository, contador, sessaoAtual);
		doThrow(new LimiteDeTentativasException("Muitas tentativas. Aguarde 5 minutos."))
				.when(contador).verificar("admin@quadrante.local");

		assertThrows(LimiteDeTentativasException.class, () -> service.entrar(
				new LoginRequest("admin@quadrante.local", "Admin123"),
				new MockHttpServletRequest(),
				new MockHttpServletResponse()));
	}

	@Test
	@DisplayName("Login válido abre sessão e devolve o perfil")
	void loginValido() {
		AuthService service = new AuthService(authenticationManager, securityContextRepository, contador, sessaoAtual);
		Estabelecimento estabelecimento = new Estabelecimento();
		estabelecimento.setId(1L);
		Usuario usuario = new Usuario();
		usuario.setId(7L);
		usuario.setEstabelecimento(estabelecimento);
		usuario.setEmail("admin@quadrante.local");
		usuario.setSenhaHash("$2a$12$hash");
		usuario.setPerfil(Perfil.ADMINISTRADOR);
		usuario.setAtivo(true);
		UsuarioDetails detalhes = new UsuarioDetails(usuario);
		when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any()))
				.thenReturn(new UsernamePasswordAuthenticationToken(detalhes, null, detalhes.getAuthorities()));

		var resposta = service.entrar(
				new LoginRequest("admin@quadrante.local", "Admin123"),
				new MockHttpServletRequest(),
				new MockHttpServletResponse());

		assertEquals(Perfil.ADMINISTRADOR, resposta.perfil());
		assertEquals(1L, resposta.estabelecimentoId());
		verify(contador).zerar("admin@quadrante.local");
		verify(securityContextRepository).saveContext(
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.any());
	}
}
