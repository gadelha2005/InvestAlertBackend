package com.investalert.investalert.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {

    private int status;
    private String erro;
    private String mensagem;
    private LocalDateTime timestamp;
    private Map<String, String> campos;
}