package com.investalert.investalert.model;

import com.investalert.investalert.model.enums.TipoEventoCarteira;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "carteira_historico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarteiraHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carteira_id", nullable = false)
    private Carteira carteira;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "valor_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 50)
    private TipoEventoCarteira tipoEvento;

    @Column(length = 255)
    private String descricao;

    @PrePersist
    protected void onCreate() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now();
        }
    }
}
