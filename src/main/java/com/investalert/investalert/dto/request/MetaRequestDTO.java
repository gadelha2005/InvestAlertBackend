package com.investalert.investalert.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class MetaRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @NotNull(message = "Valor objetivo é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor objetivo deve ser maior que zero")
    private BigDecimal valorObjetivo;

    private LocalDateTime dataLimite;
}