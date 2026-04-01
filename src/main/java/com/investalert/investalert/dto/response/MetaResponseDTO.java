package com.investalert.investalert.dto.response;

import com.investalert.investalert.model.enums.TipoAcompanhamentoMeta;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MetaResponseDTO {

    private Long id;
    private String nome;
    private BigDecimal valorObjetivo;
    private BigDecimal valorAtual;
    private TipoAcompanhamentoMeta tipoAcompanhamento;
    private Long carteiraId;
    private BigDecimal percentualConcluido;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataLimite;
}
