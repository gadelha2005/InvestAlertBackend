package com.investalert.investalert.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponseDTO {

    private String token;
    private String tipo;
    private Long usuarioId;
    private String nome;
    private String email;
}