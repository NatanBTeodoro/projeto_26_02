package com.projeto.seguranca;

import com.projeto.entity.enums.Perfil;

public record UsuarioAtual(Long id, Long estabelecimentoId, Perfil perfil, String email) {
}
