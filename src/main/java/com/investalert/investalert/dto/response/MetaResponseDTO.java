package com.investalert.investalert.dto.response;

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
    private BigDecimal percentualConcluido;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataLimite;
}