package com.investalert.investalert.controller;

import com.investalert.investalert.dto.response.DashboardResponseDTO;
import com.investalert.investalert.service.DashboardService;
import com.investalert.investalert.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.ok(dashboardService.getDashboard(usuarioId));
    }

    private Long getUsuarioId(UserDetails userDetails) {
        return usuarioService.buscarEntidadePorEmail(userDetails.getUsername()).getId();
    }
}