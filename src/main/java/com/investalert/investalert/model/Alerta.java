package com.investalert.investalert.model;

import com.investalert.investalert.model.enums.TipoAlerta;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ativo_id", nullable = false)
    private Ativo ativo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoAlerta tipo;

    @Column(name = "valor_alvo", nullable = false, precision = 19, scale = 4)
    private BigDecimal valorAlvo;

    @Column(name = "notificar_whatsapp")
    private Boolean notificarWhatsapp = false;

    @Column(nullable = false)
    private Boolean ativado = true;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @OneToMany(mappedBy = "alerta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Notificacao> notificacoes;

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }
}