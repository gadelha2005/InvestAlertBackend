package com.investalert.investalert.controller;

import com.investalert.investalert.dto.request.AtivoRequestDTO;
import com.investalert.investalert.dto.response.AtivoResponseDTO;
import com.investalert.investalert.service.AtivoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ativos")
@RequiredArgsConstructor
public class AtivoController {

    private final AtivoService ativoService;

    @PostMapping
    public ResponseEntity<AtivoResponseDTO> cadastrar(@RequestBody @Valid AtivoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ativoService.cadastrar(dto));
    }

    @GetMapping
    public ResponseEntity<List<AtivoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(ativoService.listarTodos());
    }

    @GetMapping("/{ticker}")
    public ResponseEntity<AtivoResponseDTO> buscarPorTicker(@PathVariable String ticker) {
        return ResponseEntity.ok(ativoService.buscarPorTicker(ticker));
    }
}