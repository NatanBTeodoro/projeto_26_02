package com.projeto.entity;

import java.math.BigDecimal;

import com.projeto.entity.enums.ClasseMenu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "resultado_classificacao")
public class ResultadoClassificacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "execucao_id", nullable = false)
	private AnaliseExecucao execucao;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "produto_id", nullable = false)
	private Produto produto;

	@Column(precision = 12, scale = 6)
	private BigDecimal popularidade;

	@Column(precision = 12, scale = 2)
	private BigDecimal margem;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ClasseMenu classe;
}
