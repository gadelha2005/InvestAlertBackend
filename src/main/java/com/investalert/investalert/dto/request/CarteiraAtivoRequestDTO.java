package com.investalert.investalert.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CarteiraAtivoRequestDTO {

    @NotBlank(message = "Ticker é obrigatório")
    private String ticker;

    @DecimalMin(value = "0.00000001", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade;

    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;

    @DecimalMin(value = "0.01", message = "Preço médio deve ser maior que zero")
    private BigDecimal precoMedio;
}