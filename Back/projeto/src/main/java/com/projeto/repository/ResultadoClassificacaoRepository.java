package com.projeto.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.projeto.entity.ResultadoClassificacao;
import com.projeto.entity.enums.ClasseMenu;
import com.projeto.entity.enums.Granularidade;

public interface ResultadoClassificacaoRepository extends JpaRepository<ResultadoClassificacao, Long> {

	Page<ResultadoClassificacao> findByExecucaoId(Long execucaoId, Pageable pageable);

	List<ResultadoClassificacao> findByExecucaoId(Long execucaoId, Sort sort);

	@Query("""
			select r.classe from ResultadoClassificacao r
			where r.produto.id = :produtoId
			  and r.execucao.granularidade = :granularidade
			  and r.execucao.status = com.projeto.entity.enums.StatusAnalise.CONCLUIDA
			  and r.execucao.estabelecimento.id = :estabelecimentoId
			order by r.execucao.periodoFim desc, r.execucao.id desc
			""")
	List<ClasseMenu> classesRecentes(
			@Param("produtoId") Long produtoId,
			@Param("granularidade") Granularidade granularidade,
			@Param("estabelecimentoId") Long estabelecimentoId);
}
