package com.investalert.investalert.controller;

import com.investalert.investalert.dto.request.CarteiraAtivoRequestDTO;
import com.investalert.investalert.dto.request.CarteiraRequestDTO;
import com.investalert.investalert.dto.response.CarteiraAtivoResponseDTO;
import com.investalert.investalert.dto.response.CarteiraResponseDTO;
import com.investalert.investalert.service.CarteiraService;
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
@RequestMapping("/api/carteiras")
@RequiredArgsConstructor
public class CarteiraController {

    private final CarteiraService carteiraService;
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<CarteiraResponseDTO> criar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid CarteiraRequestDTO dto) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(carteiraService.criar(usuarioId, dto));
    }

    @GetMapping
    public ResponseEntity<List<CarteiraResponseDTO>> listar(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.ok(carteiraService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarteiraResponseDTO> buscarPorId(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.ok(carteiraService.buscarPorId(id, usuarioId));
    }

    @PostMapping("/{id}/ativos")
    public ResponseEntity<CarteiraAtivoResponseDTO> adicionarAtivo(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody @Valid CarteiraAtivoRequestDTO dto) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(carteiraService.adicionarAtivo(id, usuarioId, dto));
    }

    @DeleteMapping("/{id}/ativos/{carteiraAtivoId}")
    public ResponseEntity<Void> removerAtivo(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @PathVariable Long carteiraAtivoId) {

        Long usuarioId = getUsuarioId(userDetails);
        carteiraService.removerAtivo(id, carteiraAtivoId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    private Long getUsuarioId(UserDetails userDetails) {
        return usuarioService.buscarEntidadePorEmail(userDetails.getUsername()).getId();
    }
}