package com.projeto.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "auditoria")
public class Auditoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	@Column(nullable = false, length = 80)
	private String acao;

	@Column(nullable = false, length = 80)
	private String entidade;

	@Column(name = "entidade_id")
	private Long entidadeId;

	@Column(name = "valor_anterior", columnDefinition = "TEXT")
	private String valorAnterior;

	@Column(name = "valor_novo", columnDefinition = "TEXT")
	private String valorNovo;

	@Column(name = "data_hora", nullable = false)
	private Instant dataHora = Instant.now();
}
