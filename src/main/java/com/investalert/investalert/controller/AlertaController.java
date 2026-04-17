package com.investalert.investalert.controller;

import com.investalert.investalert.config.security.UserPrincipal;
import com.investalert.investalert.dto.request.AlertaRequestDTO;
import com.investalert.investalert.dto.response.AlertaResponseDTO;
import com.investalert.investalert.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Alertas")
@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService alertaService;

    @Operation(summary = "Criar alerta")
    @PostMapping
    public ResponseEntity<AlertaResponseDTO> criar(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid AlertaRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(alertaService.criar(principal.getUsuarioId(), dto));
    }

    @Operation(summary = "Listar alertas do usuário")
    @GetMapping
    public ResponseEntity<List<AlertaResponseDTO>> listar(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(alertaService.listarPorUsuario(principal.getUsuarioId()));
    }

    @Operation(summary = "Deletar alerta")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        alertaService.deletar(id, principal.getUsuarioId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ativar/desativar alerta")
    @PatchMapping("/{id}/status")
    public ResponseEntity<AlertaResponseDTO> alterarStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestParam Boolean ativado) {

        return ResponseEntity.ok(alertaService.alterarStatus(id, principal.getUsuarioId(), ativado));
    }
}