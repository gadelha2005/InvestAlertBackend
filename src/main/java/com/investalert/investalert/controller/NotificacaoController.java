package com.investalert.investalert.controller;

import com.investalert.investalert.dto.response.NotificacaoResponseDTO;
import com.investalert.investalert.service.NotificacaoService;
import com.investalert.investalert.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<NotificacaoResponseDTO>> listar(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.ok(notificacaoService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/nao-lidas")
    public ResponseEntity<List<NotificacaoResponseDTO>> listarNaoLidas(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.ok(notificacaoService.listarNaoLidas(usuarioId));
    }

    @GetMapping("/nao-lidas/count")
    public ResponseEntity<Long> contarNaoLidas(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.ok(notificacaoService.contarNaoLidas(usuarioId));
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<NotificacaoResponseDTO> marcarComoLida(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.ok(notificacaoService.marcarComoLida(id, usuarioId));
    }

    @PatchMapping("/lidas")
    public ResponseEntity<Void> marcarTodasComoLidas(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long usuarioId = getUsuarioId(userDetails);
        notificacaoService.marcarTodasComoLidas(usuarioId);
        return ResponseEntity.noContent().build();
    }

    private Long getUsuarioId(UserDetails userDetails) {
        return usuarioService.buscarEntidadePorEmail(userDetails.getUsername()).getId();
    }
}