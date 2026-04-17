package com.investalert.investalert.service;

import com.investalert.investalert.dto.request.AlertaRequestDTO;
import com.investalert.investalert.dto.response.AlertaResponseDTO;
import com.investalert.investalert.exception.BusinessException;
import com.investalert.investalert.exception.UnauthorizedException;
import com.investalert.investalert.model.Alerta;
import com.investalert.investalert.model.Ativo;
import com.investalert.investalert.model.PrecoAtivo;
import com.investalert.investalert.model.Usuario;
import com.investalert.investalert.model.enums.TipoAlerta;
import com.investalert.investalert.model.enums.TipoAtivo;
import com.investalert.investalert.repository.AlertaRepository;
import com.investalert.investalert.repository.PrecoAtivoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private PrecoAtivoRepository precoAtivoRepository;

    @Mock
    private AtivoService ativoService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AlertaService alertaService;

    private Usuario usuario;
    private Ativo ativo;
    private PrecoAtivo precoAtivo;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .email("user@test.com")
                .nome("Teste")
                .senha("senha")
                .build();

        ativo = Ativo.builder()
                .id(10L)
                .ticker("PETR4")
                .nome("Petrobras")
                .tipo(TipoAtivo.ACAO)
                .build();

        precoAtivo = PrecoAtivo.builder()
                .id(1L)
                .ativo(ativo)
                .preco(new BigDecimal("30.00"))
                .build();
    }

    @Test
    void criar_deveRetornarAlertaComPrecoAtual() {
        AlertaRequestDTO dto = new AlertaRequestDTO();
        dto.setTicker("PETR4");
        dto.setTipo(TipoAlerta.PRECO_ACIMA);
        dto.setValorAlvo(new BigDecimal("50.00"));

        Alerta alertaSalvo = Alerta.builder()
                .id(1L)
                .usuario(usuario)
                .ativo(ativo)
                .tipo(TipoAlerta.PRECO_ACIMA)
                .valorAlvo(new BigDecimal("50.00"))
                .ativado(true)
                .condicaoDisparada(false)
                .build();

        when(usuarioService.buscarEntidadePorId(1L)).thenReturn(usuario);
        when(ativoService.buscarEntidadePorTicker("PETR4")).thenReturn(ativo);
        when(precoAtivoRepository.findTopByAtivoIdOrderByDataHoraDesc(10L))
                .thenReturn(Optional.of(precoAtivo));
        when(alertaRepository.save(any(Alerta.class))).thenReturn(alertaSalvo);

        AlertaResponseDTO resultado = alertaService.criar(1L, dto);

        assertThat(resultado.getTicker()).isEqualTo("PETR4");
        assertThat(resultado.getPrecoAtual()).isEqualByComparingTo("30.00");
        assertThat(resultado.getValorAlvo()).isEqualByComparingTo("50.00");
        verify(alertaRepository).save(any(Alerta.class));
    }

    @Test
    void criar_deveLancarExcecaoParaPrecoAcimaComValorMenorQuePrecoAtual() {
        AlertaRequestDTO dto = new AlertaRequestDTO();
        dto.setTicker("PETR4");
        dto.setTipo(TipoAlerta.PRECO_ACIMA);
        dto.setValorAlvo(new BigDecimal("10.00")); // menor que 30.00

        when(usuarioService.buscarEntidadePorId(1L)).thenReturn(usuario);
        when(ativoService.buscarEntidadePorTicker("PETR4")).thenReturn(ativo);
        when(precoAtivoRepository.findTopByAtivoIdOrderByDataHoraDesc(10L))
                .thenReturn(Optional.of(precoAtivo));

        assertThatThrownBy(() -> alertaService.criar(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("preco maximo");
    }

    @Test
    void deletar_deveLancarExcecaoSeUsuarioNaoEDono() {
        Alerta alertaDeOutroUsuario = Alerta.builder()
                .id(1L)
                .usuario(Usuario.builder().id(99L).build())
                .ativo(ativo)
                .tipo(TipoAlerta.PRECO_ACIMA)
                .valorAlvo(new BigDecimal("50.00"))
                .build();

        when(alertaRepository.findById(1L)).thenReturn(Optional.of(alertaDeOutroUsuario));

        assertThatThrownBy(() -> alertaService.deletar(1L, 1L))
                .isInstanceOf(UnauthorizedException.class);

        verify(alertaRepository, never()).delete(any());
    }

    @Test
    void alterarStatus_deveResetarCondicaoDisparadaQuandoDesativar() {
        Alerta alerta = Alerta.builder()
                .id(1L)
                .usuario(usuario)
                .ativo(ativo)
                .tipo(TipoAlerta.PRECO_ACIMA)
                .valorAlvo(new BigDecimal("50.00"))
                .ativado(true)
                .condicaoDisparada(true)
                .build();

        when(alertaRepository.findById(1L)).thenReturn(Optional.of(alerta));
        when(precoAtivoRepository.findTopByAtivoIdOrderByDataHoraDesc(10L))
                .thenReturn(Optional.of(precoAtivo));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertaResponseDTO resultado = alertaService.alterarStatus(1L, 1L, false);

        assertThat(alerta.getCondicaoDisparada()).isFalse();
        assertThat(alerta.getAtivado()).isFalse();
        verify(alertaRepository).save(alerta);
    }

    @Test
    void listarPorUsuario_deveRetornarListaComTodosAlertasDoUsuario() {
        Alerta a1 = Alerta.builder().id(1L).usuario(usuario).ativo(ativo)
                .tipo(TipoAlerta.PRECO_ACIMA).valorAlvo(new BigDecimal("50.00"))
                .ativado(true).condicaoDisparada(false).build();
        Alerta a2 = Alerta.builder().id(2L).usuario(usuario).ativo(ativo)
                .tipo(TipoAlerta.PRECO_ABAIXO).valorAlvo(new BigDecimal("20.00"))
                .ativado(true).condicaoDisparada(false).build();

        when(alertaRepository.findByUsuarioId(1L)).thenReturn(List.of(a1, a2));
        when(precoAtivoRepository.findTopByAtivoIdOrderByDataHoraDesc(10L))
                .thenReturn(Optional.of(precoAtivo));

        List<AlertaResponseDTO> resultado = alertaService.listarPorUsuario(1L);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getTicker()).isEqualTo("PETR4");
    }
}
