package com.investalert.investalert.service;

import com.investalert.investalert.dto.request.CarteiraAtivoRequestDTO;
import com.investalert.investalert.dto.request.CarteiraRequestDTO;
import com.investalert.investalert.dto.response.CarteiraAtivoResponseDTO;
import com.investalert.investalert.dto.response.CarteiraResponseDTO;
import com.investalert.investalert.exception.UnauthorizedException;
import com.investalert.investalert.integration.PrecoService;
import com.investalert.investalert.model.Ativo;
import com.investalert.investalert.model.Carteira;
import com.investalert.investalert.model.CarteiraAtivo;
import com.investalert.investalert.model.PrecoAtivo;
import com.investalert.investalert.model.Usuario;
import com.investalert.investalert.model.enums.TipoAtivo;
import com.investalert.investalert.repository.CarteiraAtivoRepository;
import com.investalert.investalert.repository.CarteiraHistoricoRepository;
import com.investalert.investalert.repository.CarteiraRepository;
import com.investalert.investalert.repository.PrecoAtivoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class CarteiraServiceTest {

    @Mock private CarteiraRepository carteiraRepository;
    @Mock private CarteiraAtivoRepository carteiraAtivoRepository;
    @Mock private PrecoAtivoRepository precoAtivoRepository;
    @Mock private CarteiraHistoricoRepository carteiraHistoricoRepository;
    @Mock private AtivoService ativoService;
    @Mock private UsuarioService usuarioService;
    @Mock private PrecoService precoService;

    @InjectMocks
    private CarteiraService carteiraService;

    private Usuario usuario;
    private Ativo ativo;
    private Carteira carteira;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(1L).email("user@test.com").nome("Teste").senha("senha").build();
        ativo = Ativo.builder().id(10L).ticker("PETR4").nome("Petrobras").tipo(TipoAtivo.ACAO).build();
        carteira = Carteira.builder().id(1L).usuario(usuario).nome("Carteira Principal").build();
    }

    @Test
    void criar_deveRetornarCarteiraComListaVazia() {
        CarteiraRequestDTO dto = new CarteiraRequestDTO();
        dto.setNome("Minha Carteira");

        when(usuarioService.buscarEntidadePorId(1L)).thenReturn(usuario);
        when(carteiraRepository.save(any(Carteira.class))).thenAnswer(inv -> {
            Carteira c = inv.getArgument(0);
            c = Carteira.builder().id(1L).usuario(usuario).nome(c.getNome()).build();
            return c;
        });
        when(carteiraHistoricoRepository.save(any())).thenReturn(null);

        CarteiraResponseDTO resultado = carteiraService.criar(1L, dto);

        assertThat(resultado.getNome()).isEqualTo("Minha Carteira");
        assertThat(resultado.getAtivos()).isEmpty();
        assertThat(resultado.getValorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void adicionarAtivo_deveCalcularPrecoMedioAoAdicionarMesmoAtivo() {
        CarteiraAtivo posicaoExistente = CarteiraAtivo.builder()
                .id(1L)
                .carteira(carteira)
                .ativo(ativo)
                .quantidade(new BigDecimal("10"))
                .precoMedio(new BigDecimal("20.00"))
                .build();

        CarteiraAtivoRequestDTO dto = new CarteiraAtivoRequestDTO();
        dto.setTicker("PETR4");
        dto.setQuantidade(new BigDecimal("10"));
        dto.setPrecoMedio(new BigDecimal("30.00"));

        when(carteiraRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(carteira));
        when(ativoService.buscarEntidadePorTicker("PETR4")).thenReturn(ativo);
        when(carteiraAtivoRepository.findByCarteiraIdAndAtivoId(1L, 10L))
                .thenReturn(Optional.of(posicaoExistente));
        when(carteiraAtivoRepository.save(any(CarteiraAtivo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(precoAtivoRepository.findTopByAtivoIdOrderByDataHoraDesc(10L))
                .thenReturn(Optional.of(PrecoAtivo.builder().preco(new BigDecimal("25.00")).ativo(ativo).build()));
        when(carteiraAtivoRepository.findByCarteiraIdWithAtivo(1L)).thenReturn(List.of());
        when(carteiraHistoricoRepository.save(any())).thenReturn(null);

        CarteiraAtivoResponseDTO resultado = carteiraService.adicionarAtivo(1L, 1L, dto);

        // Preço médio ponderado: (10*20 + 10*30) / 20 = 500/20 = 25.00
        assertThat(resultado.getPrecoMedio()).isEqualByComparingTo("25.00");
        assertThat(resultado.getQuantidade()).isEqualByComparingTo("20");
    }

    @Test
    void adicionarAtivo_deveUsarPrecoAtualQuandoSomenteQuantidadeInformada() {
        CarteiraAtivoRequestDTO dto = new CarteiraAtivoRequestDTO();
        dto.setTicker("PETR4");
        dto.setQuantidade(new BigDecimal("5"));

        when(carteiraRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(carteira));
        when(ativoService.buscarEntidadePorTicker("PETR4")).thenReturn(ativo);
        when(carteiraAtivoRepository.findByCarteiraIdAndAtivoId(1L, 10L)).thenReturn(Optional.empty());
        when(precoService.atualizarPreco(ativo)).thenReturn(Optional.of(new BigDecimal("40.00")));
        when(carteiraAtivoRepository.save(any(CarteiraAtivo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(precoAtivoRepository.findTopByAtivoIdOrderByDataHoraDesc(10L))
                .thenReturn(Optional.of(PrecoAtivo.builder().preco(new BigDecimal("40.00")).ativo(ativo).build()));
        when(carteiraAtivoRepository.findByCarteiraIdWithAtivo(1L)).thenReturn(List.of());
        when(carteiraHistoricoRepository.save(any())).thenReturn(null);

        CarteiraAtivoResponseDTO resultado = carteiraService.adicionarAtivo(1L, 1L, dto);

        assertThat(resultado.getPrecoMedio()).isEqualByComparingTo("40.00");
        assertThat(resultado.getQuantidade()).isEqualByComparingTo("5");
    }

    @Test
    void removerAtivo_deveLancarExcecaoSeCarteiraDeOutroUsuario() {
        when(carteiraRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carteiraService.removerAtivo(1L, 1L, 1L))
                .isInstanceOf(UnauthorizedException.class);

        verify(carteiraAtivoRepository, never()).delete(any());
    }

    @Test
    void listarPorUsuario_deveCalcularValorTotalCorreto() {
        CarteiraAtivo ca = CarteiraAtivo.builder()
                .id(1L)
                .carteira(carteira)
                .ativo(ativo)
                .quantidade(new BigDecimal("10"))
                .precoMedio(new BigDecimal("20.00"))
                .build();

        when(carteiraRepository.findByUsuarioId(1L)).thenReturn(List.of(carteira));
        when(carteiraAtivoRepository.findByCarteiraIdWithAtivo(1L)).thenReturn(List.of(ca));
        when(precoAtivoRepository.findTopByAtivoIdOrderByDataHoraDesc(10L))
                .thenReturn(Optional.of(PrecoAtivo.builder().preco(new BigDecimal("25.00")).ativo(ativo).build()));

        List<CarteiraResponseDTO> resultado = carteiraService.listarPorUsuario(1L);

        assertThat(resultado).hasSize(1);
        // valorAtual = 10 * 25 = 250
        assertThat(resultado.get(0).getValorTotal()).isEqualByComparingTo("250.00");
        // lucroPrejuizo = 250 - (10*20) = 250 - 200 = 50
        assertThat(resultado.get(0).getLucroPrejuizo()).isEqualByComparingTo("50.00");
    }
}
