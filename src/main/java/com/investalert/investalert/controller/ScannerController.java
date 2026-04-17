package com.investalert.investalert.controller;

import com.investalert.investalert.dto.response.AtivoResponseDTO;
import com.investalert.investalert.service.AtivoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Scanner de Mercado")
@RestController
@RequestMapping("/api/scanner")
@RequiredArgsConstructor
public class ScannerController {

    private final AtivoService ativoService;

    @GetMapping
    public ResponseEntity<List<AtivoResponseDTO>> listar() {
        return ResponseEntity.ok(ativoService.listarParaScanner());
    }
}
