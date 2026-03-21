package com.investalert.investalert.integration;

import com.investalert.investalert.model.Ativo;
import com.investalert.investalert.model.PrecoAtivo;
import com.investalert.investalert.model.enums.TipoAtivo;
import com.investalert.investalert.repository.AtivoRepository;
import com.investalert.investalert.repository.PrecoAtivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrecoService {

    private final AtivoRepository ativoRepository;
    private final PrecoAtivoRepository precoAtivoRepository;
    private final BrapiClient brapiClient;
    private final CoinGeckoClient coinGeckoClient;

    @Transactional
    public void atualizarTodosOsPrecos() {
        List<Ativo> ativos = ativoRepository.findAll();

        if (ativos.isEmpty()) {
            log.debug("Nenhum ativo cadastrado para atualizar preços.");
            return;
        }

        log.info("Iniciando atualização de preços para {} ativos.", ativos.size());

        for (Ativo ativo : ativos) {
            atualizarPreco(ativo);
        }

        log.info("Atualização de preços concluída.");
    }

    @Transactional
    public Optional<BigDecimal> atualizarPreco(Ativo ativo) {
        Optional<BigDecimal> preco = buscarPrecoExterno(ativo);

        preco.ifPresentOrElse(
                valor -> salvarPreco(ativo, valor),
                () -> log.warn("Não foi possível obter preço para o ativo: {}", ativo.getTicker())
        );

        return preco;
    }

    private Optional<BigDecimal> buscarPrecoExterno(Ativo ativo) {
        if (ativo.getTipo() == TipoAtivo.CRIPTOMOEDA) {
            return coinGeckoClient.buscarPreco(ativo.getTicker());
        }
        return brapiClient.buscarPreco(ativo.getTicker());
    }

    private void salvarPreco(Ativo ativo, BigDecimal preco) {
        PrecoAtivo precoAtivo = PrecoAtivo.builder()
                .ativo(ativo)
                .preco(preco)
                .build();

        precoAtivoRepository.save(precoAtivo);
        log.debug("Preço atualizado para {}: R$ {}", ativo.getTicker(), preco);
    }
}
