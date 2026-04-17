package com.investalert.investalert.controller;

import com.investalert.investalert.config.security.UserPrincipal;
import com.investalert.investalert.dto.response.NotificacaoResponseDTO;
import com.investalert.investalert.service.NotificacaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notificações")
@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @GetMapping
    public ResponseEntity<List<NotificacaoResponseDTO>> listar(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(notificacaoService.listarPorUsuario(principal.getUsuarioId()));
    }

    @GetMapping("/nao-lidas")
    public ResponseEntity<List<NotificacaoResponseDTO>> listarNaoLidas(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(notificacaoService.listarNaoLidas(principal.getUsuarioId()));
    }

    @GetMapping("/nao-lidas/count")
    public ResponseEntity<Long> contarNaoLidas(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(notificacaoService.contarNaoLidas(principal.getUsuarioId()));
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<NotificacaoResponseDTO> marcarComoLida(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(notificacaoService.marcarComoLida(id, principal.getUsuarioId()));
    }

    @PatchMapping("/lidas")
    public ResponseEntity<Void> marcarTodasComoLidas(
            @AuthenticationPrincipal UserPrincipal principal) {

        notificacaoService.marcarTodasComoLidas(principal.getUsuarioId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deletarTodas(
            @AuthenticationPrincipal UserPrincipal principal) {

        notificacaoService.deletarTodas(principal.getUsuarioId());
        return ResponseEntity.noContent().build();
    }
}