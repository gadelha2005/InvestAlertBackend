package com.investalert.investalert.repository;

import com.investalert.investalert.model.PrecoAtivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface PrecoAtivoRepository extends JpaRepository<PrecoAtivo, Long> {

    Optional<PrecoAtivo> findTopByAtivoIdOrderByDataHoraDesc(Long ativoId);

    List<PrecoAtivo> findTop2ByAtivoIdOrderByDataHoraDesc(Long ativoId);

    List<PrecoAtivo> findByAtivoIdInOrderByDataHoraAsc(List<Long> ativoIds);

    @Query("""
        SELECT DISTINCT pa FROM PrecoAtivo pa
        WHERE pa.ativo.id IN :ativoIds
        AND pa.dataHora = (
            SELECT MAX(pa2.dataHora) 
            FROM PrecoAtivo pa2 
            WHERE pa2.ativo.id = pa.ativo.id
        )
        ORDER BY pa.ativo.id, pa.dataHora DESC
    """)
    List<PrecoAtivo> findLatestPricesByAtivoIds(@Param("ativoIds") List<Long> ativoIds);

    @Query(value = """
        SELECT DISTINCT pa.ativo_id, pa.preco, pa.data_hora
        FROM preco_ativo pa
        WHERE pa.ativo_id IN :ativoIds
        AND (pa.ativo_id, pa.data_hora) IN (
            SELECT ativo_id, MAX(data_hora)
            FROM preco_ativo
            WHERE ativo_id IN :ativoIds
            GROUP BY ativo_id
        )
        ORDER BY pa.ativo_id
    """, nativeQuery = true)
    List<Object[]> findLatestPricesNativeByAtivoIds(@Param("ativoIds") List<Long> ativoIds);
}
