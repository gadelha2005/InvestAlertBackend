package com.investalert.investalert.controller;

import com.investalert.investalert.dto.request.UsuarioRequestDTO;
import com.investalert.investalert.dto.response.UsuarioResponseDTO;
import com.investalert.investalert.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@RequestBody @Valid UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.cadastrar(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> me(@AuthenticationPrincipal UserDetails userDetails) {
        var usuario = usuarioService.buscarEntidadePorEmail(userDetails.getUsername());
        return ResponseEntity.ok(usuarioService.buscarPorId(usuario.getId()));
    }
}