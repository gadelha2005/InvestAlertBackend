package com.investalert.investalert.repository;

import com.investalert.investalert.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByUsuarioId(Long usuarioId);

    List<Alerta> findByAtivoId(Long ativoId);

    @Query("SELECT a FROM Alerta a JOIN FETCH a.ativo WHERE a.ativado = true")
    List<Alerta> findAllAtivosWithAtivo();
}