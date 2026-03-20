package com.investalert.investalert.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class CarteiraResponseDTO {

    private Long id;
    private String nome;
    private List<CarteiraAtivoResponseDTO> ativos;
    private BigDecimal valorTotal;
    private BigDecimal lucroprejuizo;
}