package com.investalert.investalert.model;

import com.investalert.investalert.model.enums.TipoAcompanhamentoMeta;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "valor_objetivo", nullable = false, precision = 19, scale = 4)
    private BigDecimal valorObjetivo;

    @Builder.Default
    @Column(name = "valor_atual", precision = 19, scale = 4)
    private BigDecimal valorAtual = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_acompanhamento", nullable = false, length = 30)
    private TipoAcompanhamentoMeta tipoAcompanhamento = TipoAcompanhamentoMeta.MANUAL;

    @Column(name = "carteira_id")
    private Long carteiraId;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "data_limite")
    private LocalDateTime dataLimite;

    @Builder.Default
    @OneToMany(mappedBy = "meta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MetaMovimentacao> movimentacoes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }
}
