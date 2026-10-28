package com.projeto.entity;

import com.projeto.entity.enums.StatusImportacao;

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
@Table(name = "importacao")
public class Importacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "estabelecimento_id", nullable = false)
	private Estabelecimento estabelecimento;

	@Column(nullable = false, length = 255)
	private String arquivo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusImportacao status = StatusImportacao.PENDENTE;

	@Column(name = "linhas_ok", nullable = false)
	private Integer linhasOk = 0;

	@Column(name = "linhas_erro", nullable = false)
	private Integer linhasErro = 0;

	@Column(name = "relatorio_erros", columnDefinition = "TEXT")
	private String relatorioErros;
}
