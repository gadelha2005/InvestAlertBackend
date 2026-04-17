package com.investalert.investalert.service;

import com.investalert.investalert.dto.request.MetaRequestDTO;
import com.investalert.investalert.dto.response.CarteiraResponseDTO;
import com.investalert.investalert.dto.response.MetaResponseDTO;
import com.investalert.investalert.exception.UnauthorizedException;
import com.investalert.investalert.model.Meta;
import com.investalert.investalert.model.Usuario;
import com.investalert.investalert.model.enums.TipoAcompanhamentoMeta;
import com.investalert.investalert.repository.MetaRepository;
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
class MetaServiceTest {

    @Mock private MetaRepository metaRepository;
    @Mock private CarteiraService carteiraService;
    @Mock private UsuarioService usuarioService;

    @InjectMocks
    private MetaService metaService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(1L).email("user@test.com").nome("Teste").senha("senha").build();
    }

    @Test
    void criar_deveRetornarMetaComPercentualZero() {
        MetaRequestDTO dto = new MetaRequestDTO();
        dto.setNome("Aposentadoria");
        dto.setValorObjetivo(new BigDecimal("100000.00"));

        Meta metaSalva = Meta.builder()
                .id(1L)
                .usuario(usuario)
                .nome("Aposentadoria")
                .valorObjetivo(new BigDecimal("100000.00"))
                .valorAtual(BigDecimal.ZERO)
                .tipoAcompanhamento(TipoAcompanhamentoMeta.MANUAL)
                .build();

        when(usuarioService.buscarEntidadePorId(1L)).thenReturn(usuario);
        when(metaRepository.save(any(Meta.class))).thenReturn(metaSalva);

        MetaResponseDTO resultado = metaService.criar(1L, dto);

        assertThat(resultado.getNome()).isEqualTo("Aposentadoria");
        assertThat(resultado.getPercentualConcluido()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultado.getValorAtual()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void listar_deveCalcularPercentualDeConclusao() {
        Meta meta = Meta.builder()
                .id(1L)
                .usuario(usuario)
                .nome("Viagem")
                .valorObjetivo(new BigDecimal("10000.00"))
                .valorAtual(new BigDecimal("2500.00"))
                .tipoAcompanhamento(TipoAcompanhamentoMeta.MANUAL)
                .build();

        when(metaRepository.findByUsuarioIdOrderByDataLimiteAsc(1L)).thenReturn(List.of(meta));

        List<MetaResponseDTO> resultado = metaService.listarPorUsuario(1L);

        assertThat(resultado).hasSize(1);
        // 2500 / 10000 * 100 = 25%
        assertThat(resultado.get(0).getPercentualConcluido()).isEqualByComparingTo("25.0000");
    }

    @Test
    void listar_deveUsarValorCarteiraParaMetaVinculada() {
        Meta meta = Meta.builder()
                .id(1L)
                .usuario(usuario)
                .nome("Meta Carteira")
                .valorObjetivo(new BigDecimal("50000.00"))
                .valorAtual(BigDecimal.ZERO)
                .tipoAcompanhamento(TipoAcompanhamentoMeta.CARTEIRA_VINCULADA)
                .carteiraId(5L)
                .build();

        CarteiraResponseDTO carteiraDTO = CarteiraResponseDTO.builder()
                .id(5L)
                .nome("Carteira Principal")
                .valorTotal(new BigDecimal("25000.00"))
                .build();

        when(metaRepository.findByUsuarioIdOrderByDataLimiteAsc(1L)).thenReturn(List.of(meta));
        when(carteiraService.buscarPorId(5L, 1L)).thenReturn(carteiraDTO);
        when(metaRepository.save(any(Meta.class))).thenReturn(meta);

        List<MetaResponseDTO> resultado = metaService.listarPorUsuario(1L);

        assertThat(resultado.get(0).getValorAtual()).isEqualByComparingTo("25000.00");
        // 25000 / 50000 * 100 = 50%
        assertThat(resultado.get(0).getPercentualConcluido()).isEqualByComparingTo("50.0000");
    }

    @Test
    void atualizar_deveLancarExcecaoSeMetaDeOutroUsuario() {
        Meta metaDeOutroUsuario = Meta.builder()
                .id(1L)
                .usuario(Usuario.builder().id(99L).build())
                .nome("Meta Alheia")
                .valorObjetivo(new BigDecimal("1000.00"))
                .build();

        when(metaRepository.findById(1L)).thenReturn(Optional.of(metaDeOutroUsuario));

        MetaRequestDTO dto = new MetaRequestDTO();
        dto.setNome("Novo nome");
        dto.setValorObjetivo(new BigDecimal("2000.00"));

        assertThatThrownBy(() -> metaService.atualizar(1L, 1L, dto))
                .isInstanceOf(UnauthorizedException.class);

        verify(metaRepository, never()).save(any());
    }

    @Test
    void deletar_deveRemoverMetaDoUsuario() {
        Meta meta = Meta.builder()
                .id(1L)
                .usuario(usuario)
                .nome("Meta para deletar")
                .valorObjetivo(new BigDecimal("5000.00"))
                .build();

        when(metaRepository.findById(1L)).thenReturn(Optional.of(meta));

        metaService.deletar(1L, 1L);

        verify(metaRepository).delete(meta);
    }
}
