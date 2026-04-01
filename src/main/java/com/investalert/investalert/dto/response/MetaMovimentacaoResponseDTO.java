package com.investalert.investalert.dto.response;

import com.investalert.investalert.model.enums.TipoMovimentacaoMeta;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class MetaMovimentacaoResponseDTO {

    private Long id;
    private Long metaId;
    private TipoMovimentacaoMeta tipo;
    private BigDecimal valor;
    private String descricao;
    private LocalDateTime dataMovimentacao;
    private LocalDateTime dataCriacao;
}
