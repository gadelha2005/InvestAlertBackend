package com.investalert.investalert.dto.response;

import com.investalert.investalert.model.enums.TipoAlerta;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AlertaResponseDTO {

    private Long id;
    private String ticker;
    private TipoAlerta tipo;
    private BigDecimal valorAlvo;
    private BigDecimal precoAtual;
    private Boolean notificarWhatsapp;
    private Boolean ativado;
    private LocalDateTime dataCriacao;
}