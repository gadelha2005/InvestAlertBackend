package com.investalert.investalert.controller;

import com.investalert.investalert.dto.request.AlertaRequestDTO;
import com.investalert.investalert.dto.response.AlertaResponseDTO;
import com.investalert.investalert.service.AlertaService;
import com.investalert.investalert.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService alertaService;
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<AlertaResponseDTO> criar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid AlertaRequestDTO dto) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(alertaService.criar(usuarioId, dto));
    }

    @GetMapping
    public ResponseEntity<List<AlertaResponseDTO>> listar(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.ok(alertaService.listarPorUsuario(usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long usuarioId = getUsuarioId(userDetails);
        alertaService.deletar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AlertaResponseDTO> alterarStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam Boolean ativado) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.ok(alertaService.alterarStatus(id, usuarioId, ativado));
    }

    private Long getUsuarioId(UserDetails userDetails) {
        return usuarioService.buscarEntidadePorEmail(userDetails.getUsername()).getId();
    }
}