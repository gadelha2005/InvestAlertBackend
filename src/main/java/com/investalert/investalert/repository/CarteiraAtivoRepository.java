package com.investalert.investalert.repository;

import com.investalert.investalert.model.CarteiraAtivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarteiraAtivoRepository extends JpaRepository<CarteiraAtivo, Long> {

    List<CarteiraAtivo> findByCarteiraId(Long carteiraId);

    Optional<CarteiraAtivo> findByCarteiraIdAndAtivoId(Long carteiraId, Long ativoId);

    @Query("SELECT ca FROM CarteiraAtivo ca JOIN FETCH ca.ativo WHERE ca.carteira.id = :carteiraId")
    List<CarteiraAtivo> findByCarteiraIdWithAtivo(@Param("carteiraId") Long carteiraId);

    @Query("""
            SELECT ca
            FROM CarteiraAtivo ca
            JOIN FETCH ca.ativo
            JOIN FETCH ca.carteira c
            WHERE c.usuario.id = :usuarioId
            """)
    List<CarteiraAtivo> findByUsuarioIdWithAtivo(@Param("usuarioId") Long usuarioId);
}
