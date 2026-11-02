package com.projeto.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.projeto.entity.PedidoItem;

public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {

	@Query("""
			select i from PedidoItem i
			join fetch i.produto p
			left join fetch p.categoria
			join fetch i.pedido ped
			left join fetch ped.unidade
			where ped.estabelecimento.id = :estabelecimentoId
			  and ped.status = com.projeto.entity.enums.StatusPedido.ATIVO
			  and ped.dataHora >= :inicio
			  and ped.dataHora <= :fim
			""")
	List<PedidoItem> buscarAtivosNoPeriodo(
			@Param("estabelecimentoId") Long estabelecimentoId,
			@Param("inicio") Instant inicio,
			@Param("fim") Instant fim);

	@Query("""
			select i.produto.id, min(ped.dataHora)
			from PedidoItem i
			join i.pedido ped
			where ped.estabelecimento.id = :estabelecimentoId
			  and ped.status = com.projeto.entity.enums.StatusPedido.ATIVO
			group by i.produto.id
			""")
	List<Object[]> primeiraVendaPorProduto(@Param("estabelecimentoId") Long estabelecimentoId);
}
