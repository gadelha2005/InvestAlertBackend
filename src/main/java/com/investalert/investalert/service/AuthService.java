package com.investalert.investalert.service;

import com.investalert.investalert.config.security.JwtUtil;
import com.investalert.investalert.dto.request.LoginRequestDTO;
import com.investalert.investalert.dto.response.LoginResponseDTO;
import com.investalert.investalert.exception.BusinessException;
import com.investalert.investalert.exception.UnauthorizedException;
import com.investalert.investalert.integration.EmailService;
import com.investalert.investalert.model.Usuario;
import com.investalert.investalert.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Value("${frontend.url}")
    private String frontendUrl;

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

    @Transactional
    public void solicitarRedefinicao(String email) {
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuario.setResetTokenExpiracao(LocalDateTime.now().plusHours(1));
            usuarioRepository.save(usuario);

            String link = frontendUrl + "/redefinir-senha?token=" + token;
            String mensagem = "Olá, " + usuario.getNome() + "!\n\n"
                    + "Recebemos uma solicitação para redefinir sua senha no InvestAlert.\n\n"
                    + "Clique no link abaixo para criar uma nova senha (válido por 1 hora):\n"
                    + link + "\n\n"
                    + "Se você não solicitou a redefinição, ignore este email.";

            emailService.enviar(email, "Redefinição de senha - InvestAlert", mensagem);
            log.info("Email de redefinição de senha enviado para: {}", email);
        });
        // Resposta genérica independente de o email existir (segurança)
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        Usuario usuario = usuarioRepository.findByResetToken(token)
                .orElseThrow(() -> new BusinessException("Token inválido ou expirado"));

        if (usuario.getResetTokenExpiracao() == null ||
                LocalDateTime.now().isAfter(usuario.getResetTokenExpiracao())) {
            throw new BusinessException("Token inválido ou expirado");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiracao(null);
        usuarioRepository.save(usuario);
    }
}