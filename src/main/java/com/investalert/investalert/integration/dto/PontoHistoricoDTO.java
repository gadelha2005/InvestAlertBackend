package com.investalert.investalert.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PontoHistoricoDTO {
    private String dataHora;
    private BigDecimal preco;
}
