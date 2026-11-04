package com.projeto.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.projeto.config.SecurityConfig;
import com.projeto.seguranca.JsonAuthHandlers;
import com.projeto.seguranca.UsuarioDetailsService;
import com.projeto.service.UsuarioService;

@WebMvcTest(controllers = UsuarioController.class)
@AutoConfigureMockMvc
@Import({ SecurityConfig.class, JsonAuthHandlers.class })
class AcessoApiTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UsuarioService usuarioService;

	@MockitoBean
	private UsuarioDetailsService usuarioDetailsService;

	@Test
	void semSessaoRetorna401() throws Exception {
		mockMvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "GESTOR")
	void gestorEmUsuariosRetorna403() throws Exception {
		mockMvc.perform(get("/api/usuarios")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMINISTRADOR")
	void administradorEmUsuariosPassaDoFiltro() throws Exception {
		mockMvc.perform(get("/api/usuarios")).andExpect(status().isOk());
	}
}
