package com.investalert.investalert.repository;

import com.investalert.investalert.model.MetaMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetaMovimentacaoRepository extends JpaRepository<MetaMovimentacao, Long> {

    List<MetaMovimentacao> findByMetaIdOrderByDataMovimentacaoDescIdDesc(Long metaId);

    List<MetaMovimentacao> findByMetaId(Long metaId);

    Optional<MetaMovimentacao> findByIdAndMetaId(Long id, Long metaId);
}
