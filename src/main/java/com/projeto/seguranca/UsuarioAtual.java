package com.projeto.seguranca;

import com.projeto.entity.enums.Perfil;

public record UsuarioAtual(Long id, Perfil perfil, String email) {
}
