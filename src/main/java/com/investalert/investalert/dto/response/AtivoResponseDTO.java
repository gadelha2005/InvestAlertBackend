package com.investalert.investalert.dto.response;

import com.investalert.investalert.model.enums.TipoAtivo;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class AtivoResponseDTO {

    private Long id;
    private String ticker;
    private String nome;
    private TipoAtivo tipo;
    private String mercado;
    private BigDecimal precoAtual;
    private BigDecimal variacao;
    private BigDecimal variacaoPercentual;
    private Long volume;
}
