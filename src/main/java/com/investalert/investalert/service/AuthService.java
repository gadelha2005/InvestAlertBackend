package com.investalert.investalert.service;

import com.investalert.investalert.config.security.JwtUtil;
import com.investalert.investalert.dto.request.LoginRequestDTO;
import com.investalert.investalert.dto.response.LoginResponseDTO;
import com.investalert.investalert.exception.UnauthorizedException;
import com.investalert.investalert.model.Usuario;
import com.investalert.investalert.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email ou senha inválidos"));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new UnauthorizedException("Email ou senha inválidos");
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getId());

        return LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .usuarioId(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .build();
    }
}