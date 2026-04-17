package com.investalert.investalert.controller;

import com.investalert.investalert.dto.request.EsqueciSenhaRequestDTO;
import com.investalert.investalert.dto.request.LoginRequestDTO;
import com.investalert.investalert.dto.request.RedefinirSenhaRequestDTO;
import com.investalert.investalert.dto.response.LoginResponseDTO;
import com.investalert.investalert.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login", description = "Autentica o usuário e retorna um token JWT")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @Operation(summary = "Solicitar redefinição de senha", description = "Envia email com link de redefinição se o email existir")
    @PostMapping("/esqueci-senha")
    public ResponseEntity<Void> esqueciSenha(@RequestBody @Valid EsqueciSenhaRequestDTO dto) {
        authService.solicitarRedefinicao(dto.getEmail());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Redefinir senha com token")
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@RequestBody @Valid RedefinirSenhaRequestDTO dto) {
        authService.redefinirSenha(dto.getToken(), dto.getNovaSenha());
        return ResponseEntity.noContent().build();
    }
}