package com.investalert.investalert.service;

import com.investalert.investalert.dto.request.CarteiraAtivoRequestDTO;
import com.investalert.investalert.dto.request.CarteiraRequestDTO;
import com.investalert.investalert.dto.response.CarteiraAtivoResponseDTO;
import com.investalert.investalert.dto.response.CarteiraHistoricoResponseDTO;
import com.investalert.investalert.dto.response.CarteiraResponseDTO;
import com.investalert.investalert.exception.BusinessException;
import com.investalert.investalert.exception.ResourceNotFoundException;
import com.investalert.investalert.exception.UnauthorizedException;
import com.investalert.investalert.integration.PrecoService;
import com.investalert.investalert.model.Ativo;
import com.investalert.investalert.model.Carteira;
import com.investalert.investalert.model.CarteiraAtivo;
import com.investalert.investalert.model.CarteiraHistorico;
import com.investalert.investalert.model.PrecoAtivo;
import com.investalert.investalert.model.Usuario;
import com.investalert.investalert.model.enums.TipoEventoCarteira;
import com.investalert.investalert.repository.CarteiraAtivoRepository;
import com.investalert.investalert.repository.CarteiraHistoricoRepository;
import com.investalert.investalert.repository.CarteiraRepository;
import com.investalert.investalert.repository.PrecoAtivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarteiraService {

    private final CarteiraRepository carteiraRepository;
    private final CarteiraAtivoRepository carteiraAtivoRepository;
    private final PrecoAtivoRepository precoAtivoRepository;
    private final CarteiraHistoricoRepository carteiraHistoricoRepository;
    private final AtivoService ativoService;
    private final UsuarioService usuarioService;
    private final PrecoService precoService;

    @Transactional
    public CarteiraResponseDTO criar(Long usuarioId, CarteiraRequestDTO dto) {
        Usuario usuario = usuarioService.buscarEntidadePorId(usuarioId);

        Carteira carteira = Carteira.builder()
                .usuario(usuario)
                .nome(dto.getNome())
                .build();

        Carteira salva = carteiraRepository.save(carteira);
        registrarHistorico(salva, TipoEventoCarteira.CARTEIRA_CRIADA, BigDecimal.ZERO, "Carteira criada");
        return toResponse(salva, List.of());
    }

    @Transactional(readOnly = true)
    public List<CarteiraResponseDTO> listarPorUsuario(Long usuarioId) {
        return carteiraRepository.findByUsuarioId(usuarioId).stream()
                .map(carteira -> {
                    List<CarteiraAtivo> itens = carteiraAtivoRepository
                            .findByCarteiraIdWithAtivo(carteira.getId());
                    return toResponse(carteira, itens);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public CarteiraResponseDTO buscarPorId(Long carteiraId, Long usuarioId) {
        Carteira carteira = carteiraRepository.findByIdAndUsuarioId(carteiraId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira", carteiraId));

        List<CarteiraAtivo> itens = carteiraAtivoRepository
                .findByCarteiraIdWithAtivo(carteira.getId());

        return toResponse(carteira, itens);
    }

    @Transactional
    public CarteiraAtivoResponseDTO adicionarAtivo(Long carteiraId, Long usuarioId,
                                                   CarteiraAtivoRequestDTO dto) {
        Carteira carteira = carteiraRepository.findByIdAndUsuarioId(carteiraId, usuarioId)
                .orElseThrow(() -> new UnauthorizedException("Carteira nao encontrada ou sem permissao"));

        if ((dto.getQuantidade() == null || dto.getQuantidade().signum() <= 0) &&
                (dto.getValor() == null || dto.getValor().signum() <= 0) &&
                (dto.getPrecoMedio() == null || dto.getPrecoMedio().signum() <= 0)) {
            throw new BusinessException("Informe quantidade, valor ou preco medio");
        }

        Ativo ativo = ativoService.buscarEntidadePorTicker(dto.getTicker());
        Optional<CarteiraAtivo> carteiraAtivoExistente = carteiraAtivoRepository
                .findByCarteiraIdAndAtivoId(carteiraId, ativo.getId());

        if (dto.getPrecoMedio() != null && dto.getPrecoMedio().signum() > 0
                && dto.getQuantidade() != null && dto.getQuantidade().signum() > 0) {

            CarteiraAtivo salvo = salvarOuAtualizarPosicao(
                    carteira,
                    ativo,
                    carteiraAtivoExistente,
                    dto.getQuantidade(),
                    dto.getPrecoMedio()
            );

            BigDecimal valorTotalManual = calcularValorTotalCarteira(carteiraId);
            registrarHistorico(carteira, TipoEventoCarteira.ATIVO_ADICIONADO, valorTotalManual,
                    "Ativo adicionado: " + ativo.getTicker());
            return toAtivoResponse(salvo, buscarPrecoAtual(ativo.getId()));
        }

        BigDecimal precoAtualBuscado = precoService.atualizarPreco(ativo)
                .orElseGet(() -> buscarPrecoAtual(ativo.getId()));

        if (precoAtualBuscado == null) {
            throw new BusinessException(
                    "Nao foi possivel obter o preco atual para o ativo " + dto.getTicker()
                            + ". Adicione-o pelo modo manual informando quantidade e preco medio."
            );
        }

        BigDecimal quantidade;
        BigDecimal precoMedio;

        if (dto.getQuantidade() != null && dto.getQuantidade().signum() > 0) {
            quantidade = dto.getQuantidade();
            precoMedio = precoAtualBuscado;
        } else if (dto.getValor() != null && dto.getValor().signum() > 0) {
            quantidade = dto.getValor()
                    .divide(precoAtualBuscado, 8, RoundingMode.HALF_UP);
            precoMedio = precoAtualBuscado;
        } else {
            throw new BusinessException("Informe quantidade ou valor para o ativo");
        }

        CarteiraAtivo salvo = salvarOuAtualizarPosicao(
                carteira,
                ativo,
                carteiraAtivoExistente,
                quantidade,
                precoMedio
        );

        BigDecimal precoFinal = buscarPrecoAtual(ativo.getId());
        BigDecimal valorTotal = calcularValorTotalCarteira(carteiraId);
        registrarHistorico(carteira, TipoEventoCarteira.ATIVO_ADICIONADO, valorTotal,
                "Ativo adicionado: " + ativo.getTicker());

        return toAtivoResponse(salvo, precoFinal);
    }

    @Transactional
    public void removerAtivo(Long carteiraId, Long carteiraAtivoId, Long usuarioId) {
        Carteira carteira = carteiraRepository.findByIdAndUsuarioId(carteiraId, usuarioId)
                .orElseThrow(() -> new UnauthorizedException("Carteira nao encontrada ou sem permissao"));

        CarteiraAtivo carteiraAtivo = carteiraAtivoRepository.findById(carteiraAtivoId)
                .orElseThrow(() -> new ResourceNotFoundException("CarteiraAtivo", carteiraAtivoId));

        String ticker = carteiraAtivo.getAtivo().getTicker();
        carteiraAtivoRepository.delete(carteiraAtivo);

        BigDecimal valorTotal = calcularValorTotalCarteira(carteiraId);
        registrarHistorico(carteira, TipoEventoCarteira.ATIVO_REMOVIDO, valorTotal,
                "Ativo removido: " + ticker);
    }

    @Transactional(readOnly = true)
    public List<CarteiraHistoricoResponseDTO> buscarHistorico(Long carteiraId, Long usuarioId) {
        carteiraRepository.findByIdAndUsuarioId(carteiraId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira", carteiraId));

        return carteiraHistoricoRepository.findByCarteiraIdOrderByDataHoraAsc(carteiraId).stream()
                .map(h -> CarteiraHistoricoResponseDTO.builder()
                        .id(h.getId())
                        .dataHora(h.getDataHora())
                        .valorTotal(h.getValorTotal())
                        .tipoEvento(h.getTipoEvento())
                        .descricao(h.getDescricao())
                        .build())
                .toList();
    }

    private void registrarHistorico(Carteira carteira, TipoEventoCarteira tipo,
                                    BigDecimal valorTotal, String descricao) {
        CarteiraHistorico historico = CarteiraHistorico.builder()
                .carteira(carteira)
                .usuario(carteira.getUsuario())
                .valorTotal(valorTotal)
                .tipoEvento(tipo)
                .descricao(descricao)
                .build();
        carteiraHistoricoRepository.save(historico);
    }

    private BigDecimal calcularValorTotalCarteira(Long carteiraId) {
        List<CarteiraAtivo> itens = carteiraAtivoRepository.findByCarteiraIdWithAtivo(carteiraId);
        return itens.stream()
                .map(ca -> {
                    BigDecimal preco = buscarPrecoAtual(ca.getAtivo().getId());
                    if (preco == null) return BigDecimal.ZERO;
                    return preco.multiply(ca.getQuantidade());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal buscarPrecoAtual(Long ativoId) {
        return precoAtivoRepository.findTopByAtivoIdOrderByDataHoraDesc(ativoId)
                .map(PrecoAtivo::getPreco)
                .orElse(null);
    }

    private CarteiraAtivo salvarOuAtualizarPosicao(Carteira carteira,
                                                   Ativo ativo,
                                                   Optional<CarteiraAtivo> carteiraAtivoExistente,
                                                   BigDecimal quantidadeAdicional,
                                                   BigDecimal precoMedioAdicional) {
        if (carteiraAtivoExistente.isEmpty()) {
            CarteiraAtivo carteiraAtivo = CarteiraAtivo.builder()
                    .carteira(carteira)
                    .ativo(ativo)
                    .quantidade(quantidadeAdicional)
                    .precoMedio(precoMedioAdicional)
                    .build();

            return carteiraAtivoRepository.save(carteiraAtivo);
        }

        CarteiraAtivo carteiraAtivo = carteiraAtivoExistente.get();
        BigDecimal quantidadeAtual = carteiraAtivo.getQuantidade();
        BigDecimal quantidadeTotal = quantidadeAtual.add(quantidadeAdicional);

        BigDecimal valorInvestidoAtual = carteiraAtivo.getPrecoMedio().multiply(quantidadeAtual);
        BigDecimal valorInvestidoAdicional = precoMedioAdicional.multiply(quantidadeAdicional);
        BigDecimal precoMedioPonderado = valorInvestidoAtual
                .add(valorInvestidoAdicional)
                .divide(quantidadeTotal, 4, RoundingMode.HALF_UP);

        carteiraAtivo.setQuantidade(quantidadeTotal);
        carteiraAtivo.setPrecoMedio(precoMedioPonderado);

        return carteiraAtivoRepository.save(carteiraAtivo);
    }

    private CarteiraResponseDTO toResponse(Carteira carteira, List<CarteiraAtivo> itens) {
        List<CarteiraAtivoResponseDTO> ativosDTO = itens.stream()
                .map(ca -> toAtivoResponse(ca, buscarPrecoAtual(ca.getAtivo().getId())))
                .toList();

        BigDecimal valorTotal = ativosDTO.stream()
                .filter(a -> a.getValorAtual() != null)
                .map(CarteiraAtivoResponseDTO::getValorAtual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorInvestidoTotal = ativosDTO.stream()
                .map(CarteiraAtivoResponseDTO::getValorInvestido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lucroPrejuizo = valorTotal.subtract(valorInvestidoTotal);

        return CarteiraResponseDTO.builder()
                .id(carteira.getId())
                .nome(carteira.getNome())
                .ativos(ativosDTO)
                .valorTotal(valorTotal)
                .lucroPrejuizo(lucroPrejuizo)
                .build();
    }

    private CarteiraAtivoResponseDTO toAtivoResponse(CarteiraAtivo ca, BigDecimal precoAtual) {
        BigDecimal valorInvestido = ca.getPrecoMedio().multiply(ca.getQuantidade());
        BigDecimal valorAtual = precoAtual != null
                ? precoAtual.multiply(ca.getQuantidade())
                : null;
        BigDecimal lucroPrejuizo = valorAtual != null
                ? valorAtual.subtract(valorInvestido)
                : null;
        BigDecimal variacaoPercentual = null;

        if (lucroPrejuizo != null && valorInvestido.compareTo(BigDecimal.ZERO) != 0) {
            variacaoPercentual = lucroPrejuizo
                    .divide(valorInvestido, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return CarteiraAtivoResponseDTO.builder()
                .id(ca.getId())
                .ticker(ca.getAtivo().getTicker())
                .nomeAtivo(ca.getAtivo().getNome())
                .quantidade(ca.getQuantidade())
                .precoMedio(ca.getPrecoMedio())
                .precoAtual(precoAtual)
                .valorInvestido(valorInvestido)
                .valorAtual(valorAtual)
                .lucroPrejuizo(lucroPrejuizo)
                .variacaoPercentual(variacaoPercentual)
                .dataCompra(ca.getDataCompra())
                .build();
    }
}
