package com.investalert.investalert.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CarteiraAtivoResponseDTO {

    private Long id;
    private String ticker;
    private String nomeAtivo;
    private BigDecimal quantidade;
    private BigDecimal precoMedio;
    private BigDecimal precoAtual;
    private BigDecimal valorInvestido;
    private BigDecimal valorAtual;
    private BigDecimal lucroPrejuizo;
    private BigDecimal variacaoPercentual;
    private LocalDateTime dataCompra;
}